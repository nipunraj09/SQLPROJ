import os
import time
import pandas as pd
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.chrome.service import Service
from webdriver_manager.chrome import ChromeDriverManager

# Define the URL within the code
URL = "https://www.tenderdetail.com/dailytenders/43038144/ce4a5060-c8c9-44ac-867c-24f591c23c5a"  # Replace with the actual tender URL

# Step 1: Scrape the Tender Details
def scrape_tender_data(url):
    options = webdriver.ChromeOptions()
    driver = webdriver.Chrome(service=Service(ChromeDriverManager().install()), options=options)
    driver.get(url)
    
    time.sleep(5)  # Wait for page to load
    
    # Extract all tender information
    tender_data_list = []
    try:
        elements = driver.find_elements(By.XPATH, "//table//tr")  # Assuming data is in table rows
        tender_data = {}
        for row in elements:
            cells = row.find_elements(By.TAG_NAME, "td")
            if len(cells) >= 2:
                key = cells[0].text.strip()
                value = cells[1].text.strip()
                tender_data[key] = value
        
        # Extract all instances of div with class "m-mainTR"
        class_elements = driver.find_elements(By.CLASS_NAME, "m-mainTR")
        for index, elem in enumerate(class_elements, start=1):
            tender_data_copy = tender_data.copy()
            tender_data_copy["Section"] = f"Section {index}"
            tender_data_copy["Content"] = elem.text.strip()
            tender_data_list.append(tender_data_copy)
        
    except Exception as e:
        print("Error scraping data:", e)
    
    driver.quit()
    return tender_data_list

# Step 2: Save to Excel
def save_to_excel(tender_data_list):
    df = pd.DataFrame(tender_data_list)
    file_name = "Tender_Details.xlsx"
    if os.path.exists(file_name):
        existing_df = pd.read_excel(file_name)
        df = pd.concat([existing_df, df], ignore_index=True)
    df.to_excel(file_name, index=False)
    print("Data saved to Excel")

# Main Execution
def main():
    print(f"Scraping: {URL}")
    tender_info_list = scrape_tender_data(URL)
    save_to_excel(tender_info_list)
    print("Scraping complete!")

if __name__ == "__main__":
    main()
