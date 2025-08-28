import cv2
import numpy as np
import matplotlib.pyplot as plt
import os
import glob

def deskew_omr(image_path, max_angle=15):
    # Load image
    image = cv2.imread(image_path)
    if image is None:
        print(f"[WARN] Could not load {image_path}")
        return None, 0
    
    gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)

    # Preprocess: blur + binary
    blur = cv2.GaussianBlur(gray, (5,5), 0)
    _, thresh = cv2.threshold(blur, 0, 255,
                              cv2.THRESH_BINARY + cv2.THRESH_OTSU)

    # Find contours (external = sheet border)
    contours, _ = cv2.findContours(thresh, cv2.RETR_EXTERNAL,
                                   cv2.CHAIN_APPROX_SIMPLE)
    if not contours:
        print(f"[WARN] No contours found in {image_path}")
        return image, 0
    
    cnt = max(contours, key=cv2.contourArea)

    # Fit rotated rectangle around sheet
    rect = cv2.minAreaRect(cnt)
    angle = rect[-1]

    # Convert OpenCV angle to deskew angle
    if angle < -45:
        angle = 90 + angle
    elif angle > 45:
        angle = angle - 90

    # Clamp to avoid 90° rotations
    if abs(angle) > max_angle:
        angle = 0

    # Rotate image
    (h, w) = image.shape[:2]
    center = (w // 2, h // 2)
    M = cv2.getRotationMatrix2D(center, angle, 1.0)
    rotated = cv2.warpAffine(image, M, (w, h),
                             flags=cv2.INTER_CUBIC,
                             borderMode=cv2.BORDER_REPLICATE)

    return rotated, angle


def process_directory(input_dir, output_dir="output_deskewed", max_angle=15):
    os.makedirs(output_dir, exist_ok=True)

    # Scan for image files
    image_files = glob.glob(os.path.join(input_dir, "*.jpg")) \
                 + glob.glob(os.path.join(input_dir, "*.jpeg")) \
                 + glob.glob(os.path.join(input_dir, "*.png"))

    if not image_files:
        print("No image files found in", input_dir)
        return

    for img_path in image_files:
        rotated, angle = deskew_omr(img_path, max_angle=max_angle)
        if rotated is not None:
            filename = os.path.basename(img_path)
            out_path = os.path.join(output_dir, filename)
            cv2.imwrite(out_path, rotated)
            print(f"[OK] {filename} → Deskewed by {angle:.2f}° → Saved to {out_path}")
        else:
            print(f"[FAIL] {img_path} could not be processed.")


# ---------- RUN ----------
if __name__ == "__main__":
    input_directory = "input_sheets"   # <-- put your folder name here
    process_directory(input_directory, output_dir="deskewed_sheets", max_angle=15)
