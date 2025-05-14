from flask import Flask, request, jsonify
from flask_cors import CORS
from tensorflow.keras.models import load_model
import xgboost as xgb
import pickle
import numpy as np
from PIL import Image
import joblib
from sklearn.decomposition import PCA
import numpy as np
import io

def load_models(file):
    with open(file, 'rb') as f:
        return pickle.load(f)

model_n = load_models("nitrogen.pkl")
model_p = load_models("phosphorus.pkl")
model_k = load_models("potassium.pkl")
model_pH = load_models("pH.pkl")
recommendation_model = joblib.load("crop_recommendation_model.pkl")

cnn = load_model("cnn_features.h5")
vgg16 = load_model("vgg16_features.h5")

soil_detection = load_model("soil_detection_model.h5", compile=False)

cnn_pca = joblib.load('cnn_pca.pkl')
vgg16_pca = joblib.load('vgg16_pca.pkl')

soil_detection.compile(optimizer="adam", loss="binary_crossentropy", metrics=["accuracy"])

print("PCA components (VGG16):", vgg16_pca.n_components_)
print("PCA components (CNN):", cnn_pca.n_components_)

app = Flask(__name__)
CORS(app)

def preprocess_image(file):
    image = Image.open(file)
    image = image.resize((224, 224))
    image = image.convert('RGB') 
    image_array = np.array(image) / 255.0
    image_array = np.expand_dims(image_array, axis=0)
    return image_array

@app.route('/')
def index():
    return "Hello World"
    
@app.route('/soil-detection', methods=['POST'])
def detect_soil():
    if 'file' not in request.files:
        return jsonify({
            'status': 'error',
            'errorCode': 400,
            'message': 'No file part'
        }), 400
    
    file = request.files['file']
    if file.filename == '':
        return jsonify({
            'status': 'error',
            'errorCode': 400,
            'message': 'No selected file'
        }), 400
    
    try:
        image_array = preprocess_image(file)
        cnn_pred = soil_detection.predict(image_array)
        predicted_class = "soil" if cnn_pred[0][0] > 0.5 else "not soil"
        
        if predicted_class == "not soil":
            return jsonify({
                'status': 'success',
                'errorCode': 201,
                'message': 'Image does not contain soil',
                'predicted_class': predicted_class
            }), 200
        elif predicted_class == "soil":
            return jsonify({
                'status': 'sucess',
                'errorCode': 202,
                'message': "Image does contain soil",
                'predicated_class': predicted_class
            }), 200
        else :
            return jsonify({
                'status': 'error',
                'errorCode': 400,
                'message': "Bad Request"
            }), 400

    except Exception as e:
        return jsonify({
            'status': 'error',
            'errorCode': 500,
            'message': 'Internal server error',
            'details': str(e)
        }), 500

@app.route("/predict", methods=['POST'])
def predict():
    if 'file' not in request.files:
        return jsonify({
            'status': 'error',
            'errorCode': 400,
            'message': 'No file part'
        }), 400
    
    file = request.files['file']
    if file.filename == '':
        return jsonify({
            'status': 'error',
            'errorCode': 400,
            'message': 'No selected file'
        }), 400

    try:
        print("Starting image preprocessing...")
        # Read image from memory
        image_stream = io.BytesIO(file.read())
        image_array = preprocess_image(image_stream)  # Ensure this function works with in-memory images
        print("Image preprocessing complete. Shape:", image_array.shape)

        print("Predicting features using ResNet...")
        # Predict features using ResNet
        vgg16_pred = vgg16.predict(image_array)
        cnn_pred = cnn.predict(image_array)

        print("CNN prediction complete. Shape:", cnn_pred.shape)
        print("VGG16 prediction complete. Shape:", vgg16_pred.shape)

        # Flatten and reshape feature vector
        vgg16_input = vgg16_pred.flatten().reshape(1, -1)
        print("Model input prepared. Shape:", vgg16_input.shape)
        # Flatten and reshape feature vector
        cnn_input = cnn_pred.flatten().reshape(1, -1)
        print("Model input prepared. Shape:", cnn_input.shape)

        print("Applying PCA transformation...")
        pca_vgg16_features = vgg16_pca.transform(vgg16_input)  # Apply PCA
        pca_cnn_features = cnn_pca.transform(cnn_input)  # Apply PCA

        print("PCA transformation complete. Shape:", pca_vgg16_features.shape)
        print("PCA transformation complete. Shape:", pca_cnn_features.shape)

        print("Predicting soil nutrient values...")
        # Predict soil nutrient values
        model_n_pred = float(model_n.predict(pca_vgg16_features)[0])
        model_p_pred = float(model_p.predict(pca_cnn_features)[0])
        model_k_pred = float(model_k.predict(pca_vgg16_features)[0])
        model_pH_pred = float(model_pH.predict(pca_cnn_features)[0])

        print(f"Predictions - N: {model_n_pred}, P: {model_p_pred}, K: {model_k_pred}, pH: {model_pH_pred}")

        print("Preparing input for crop recommendation model...")
        # Prepare input for crop recommendation model
        features = np.array([[model_n_pred, model_p_pred, model_k_pred, model_pH_pred]])
        print("Features prepared:", features)

        print("Predicting recommended crop...")
        recommended_crop = recommendation_model.predict(features)[0]  # Predict crop
        print("Crop recommendation complete:", recommended_crop)

        return jsonify({
            'status': 'success',
            'errorCode': 203,
            'message': 'Predictions successful',
            'n': model_n_pred,
            'p': model_p_pred,
            'k': model_k_pred,
            'pH': model_pH_pred,
            'crop': recommended_crop
        })

    except Exception as e:
        print("Error occurred:", str(e))  # Print error for debugging
        return jsonify({
            'status': 'error',
            'errorCode': 500,
            'message': 'Internal server error',
            'details': str(e)
        }), 500


if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=5000)
