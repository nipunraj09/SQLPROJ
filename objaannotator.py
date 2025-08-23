from sahi.predict import get_sliced_prediction
from sahi.models.ultralytics import UltralyticsDetectionModel

import cv2

# Load trained YOLOv8 model
detection_model = UltralyticsDetectionModel(
    model_path="runs/detect/train2/weights/best.pt",  # trained model path
    confidence_threshold=0.25,
    device="mps"  # or "cpu"
)

# Run sliced inference
result = get_sliced_prediction(
    "7724.jpg",   # large input image
    detection_model,
    slice_height=320,   # slice size
    slice_width=320,
    overlap_height_ratio=0.4,
    overlap_width_ratio=0.4
)

# Export results
result.export_visuals(export_dir="sahi_outputs/",
                      hide_labels=True)
