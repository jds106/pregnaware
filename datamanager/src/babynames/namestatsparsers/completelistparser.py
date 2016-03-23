import pandas as pd


# Parses the full list of baby names
def parse(year: int, gender: str, df: pd.DataFrame) -> pd.DataFrame:
    df = df.loc[4:, ['Unnamed: 2', 'Unnamed: 3']].dropna()
    df.columns=['Name', 'Count']
    df['Year'] = year
    df['Gender'] = gender

    return df
