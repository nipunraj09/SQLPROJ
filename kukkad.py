import cv2
import numpy as np
from ultralytics import YOLO

# Load YOLOv8 model
model = YOLO("runs/detect/train10/weights/best.pt")  # replace with your weights path

# Parameters
tile_size = 640        # tile size
overlap = 0.2          # 20% overlap
confidence_threshold = 0.3

# Load full OMR sheet
img = cv2.imread("7724.jpg")
H, W = img.shape[:2]
stride = int(tile_size * (1 - overlap))

# Collect detections
all_boxes = []

# Sliding window over image
for y in range(0, H, stride):
    for x in range(0, W, stride):
        x_end = min(x + tile_size, W)
        y_end = min(y + tile_size, H)
        tile = img[y:y_end, x:x_end]

        results = model(tile)[0]  # detect on tile

        # Convert tile coords to full image coords
        for box, conf, cls in zip(results.boxes.xyxy, results.boxes.conf, results.boxes.cls):
            if conf < confidence_threshold:
                continue
            x1, y1, x2, y2 = map(int, box)
            all_boxes.append([x1 + x, y1 + y, x2 + x, y2 + y, float(conf), int(cls)])

# Non-Max Suppression to remove duplicates
def nms(boxes, iou_thresh=0.5):
    if len(boxes) == 0:
        return []
    boxes = np.array(boxes)
    x1, y1, x2, y2, scores = boxes[:,0], boxes[:,1], boxes[:,2], boxes[:,3], boxes[:,4]
    indices = scores.argsort()[::-1]
    keep = []

    while len(indices) > 0:
        i = indices[0]
        keep.append(i)
        xx1 = np.maximum(x1[i], x1[indices[1:]])
        yy1 = np.maximum(y1[i], y1[indices[1:]])
        xx2 = np.minimum(x2[i], x2[indices[1:]])
        yy2 = np.minimum(y2[i], y2[indices[1:]])

        w = np.maximum(0, xx2 - xx1)
        h = np.maximum(0, yy2 - yy1)
        inter = w
