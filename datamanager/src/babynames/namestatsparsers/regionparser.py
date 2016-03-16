import pandas as pd
import math


# Parses names by region (i.e. usual residence of mother):
#
#   North East
#   North West
#   Yorkshire and The Humber
#   East Midlands
#   West Midlands
#   East
#   London
#   South East
#   South West
#   Wales
def parse(year: int, gender: str, df: pd.DataFrame) -> pd.DataFrame:
    region_df = pd.DataFrame(columns=['Name', 'Count', 'Region'])

    df = df.loc[3:31, 'Unnamed: 1': 'Unnamed: 19'].dropna(axis=0, how='all').reset_index()

    region_df = region_df.append(parse_region(df, 0, 11, ['Unnamed: 1', 'Unnamed: 2', 'Unnamed: 3']))
    region_df = region_df.append(parse_region(df, 0, 11, ['Unnamed: 5', 'Unnamed: 6', 'Unnamed: 7']))
    region_df = region_df.append(parse_region(df, 0, 11, ['Unnamed: 9', 'Unnamed: 10', 'Unnamed: 11']))
    region_df = region_df.append(parse_region(df, 0, 11, ['Unnamed: 13', 'Unnamed: 14', 'Unnamed: 15']))
    region_df = region_df.append(parse_region(df, 0, 11, ['Unnamed: 17', 'Unnamed: 18', 'Unnamed: 19']))

    region_df = region_df.append(parse_region(df, 13, 24, ['Unnamed: 1', 'Unnamed: 2', 'Unnamed: 3']))
    region_df = region_df.append(parse_region(df, 13, 24, ['Unnamed: 5', 'Unnamed: 6', 'Unnamed: 7']))
    region_df = region_df.append(parse_region(df, 13, 24, ['Unnamed: 9', 'Unnamed: 10', 'Unnamed: 11']))
    region_df = region_df.append(parse_region(df, 13, 24, ['Unnamed: 13', 'Unnamed: 14', 'Unnamed: 15']))
    region_df = region_df.append(parse_region(df, 13, 24, ['Unnamed: 17', 'Unnamed: 18', 'Unnamed: 19']))

    region_df['Year'] = year
    region_df['Gender'] = gender

    return region_df


# Pull the relevant name data from the specified DataFrame slice
def parse_region(df: pd.DataFrame, min_row: int, max_row: int, cols: list) -> pd.DataFrame:
    df = df.loc[min_row:max_row, cols]

    # Region is in either 0,0 or 0,1 of the sliced DataFrame, with the data starting from the 3rd row
    region = df.iloc[0, 0]
    if type(region) == float:
        region = df.iloc[0, 1]

    df = df.dropna(axis=0, how='all').iloc[2:, 1:]
    df.columns = ['Name', 'Count']
    df['Region'] = region
    return df
