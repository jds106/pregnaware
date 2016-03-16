import pandas as pd


# Parse the DataFrame for monthly statistics
def parse(year: int, gender: str, df: pd.DataFrame) -> pd.DataFrame:
    months = pd.DataFrame(columns=['Month', 'Name', 'Count'])

    df = df.loc[1:, 'Unnamed: 2': 'Unnamed: 15'].dropna(axis=0, how='all').reset_index()

    months = months.append(parse_month('Jan', df, 3, 12, ['Unnamed: 2', 'Unnamed: 3']),   ignore_index=True)
    months = months.append(parse_month('Feb', df, 3, 12, ['Unnamed: 6', 'Unnamed: 7']),   ignore_index=True)
    months = months.append(parse_month('Mar', df, 3, 12, ['Unnamed: 10', 'Unnamed: 11']), ignore_index=True)
    months = months.append(parse_month('Apr', df, 3, 12, ['Unnamed: 14', 'Unnamed: 15']), ignore_index=True)

    months = months.append(parse_month('May', df, 16, 25, ['Unnamed: 2', 'Unnamed: 3']),   ignore_index=True)
    months = months.append(parse_month('Jun', df, 16, 25, ['Unnamed: 6', 'Unnamed: 7']),   ignore_index=True)
    months = months.append(parse_month('Jul', df, 16, 25, ['Unnamed: 10', 'Unnamed: 11']), ignore_index=True)
    months = months.append(parse_month('Aug', df, 16, 25, ['Unnamed: 14', 'Unnamed: 15']), ignore_index=True)

    months = months.append(parse_month('Sep', df, 29, 38, ['Unnamed: 2', 'Unnamed: 3']),   ignore_index=True)
    months = months.append(parse_month('Oct', df, 29, 38, ['Unnamed: 6', 'Unnamed: 7']),   ignore_index=True)
    months = months.append(parse_month('Nov', df, 29, 38, ['Unnamed: 10', 'Unnamed: 11']), ignore_index=True)
    months = months.append(parse_month('Dec', df, 29, 38, ['Unnamed: 14', 'Unnamed: 15']), ignore_index=True)

    months['Year'] = year
    months['Gender'] = gender

    return months


# Extract the data for the specific month
def parse_month(month: str, df: pd.DataFrame, min_row: int, max_row: int, cols: list) -> pd.DataFrame:
    month_df = df.loc[min_row: max_row, cols].dropna(how='all')
    month_df.columns = ['Name', 'Count']
    month_df['Month'] = month
    return month_df


