import pandas as pd
from simpledbf import Dbf5  # Install using: pip install simpledbf

# Load DBF file
dbf = Dbf5("your_file.dbf")
df = dbf.to_dataframe()

# Ensure 'Roll' is a string and strip spaces
df['Roll'] = df['Roll'].astype(str).str.replace(r'\s+', '', regex=True)

# Check if Roll is exactly 10 digits after modification
df['Valid_Roll'] = df['Roll'].apply(lambda x: len(x) == 10 and x.isdigit())

# Display invalid rolls
invalid_rolls = df[~df['Valid_Roll']]
print("Invalid Roll Numbers:")
print(invalid_rolls[['Roll']])

# Save cleaned data back to a CSV or Excel file
df.to_csv("cleaned_data.csv", index=False)
