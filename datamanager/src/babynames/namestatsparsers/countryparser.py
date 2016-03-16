import pandas as pd


# Parses names by country:
#   England & Wales (E&W)
#   England
#   Wales
def parse(year: int, gender: str, country: str, df: pd.DataFrame) -> pd.DataFrame:
    try:
        df = df.dropna(axis=1, how='all').iloc[5:,1:].dropna(axis=0, how='all')

        # Pull out the columns (since there are two blocks of names, this creates Name_0, Name_1 etc.)
        unique_columns = []
        for column_name in list(df.iloc[0,:]):
            index = 0
            column = '{0}_{1}'.format(column_name, index)
            while column in unique_columns:
                index += 1
                column = '{0}_{1}'.format(column_name, index)

            unique_columns.append(column)

        df = df.iloc[1:,:]
        df.columns = unique_columns

        first_50 = df[['Name_0', 'Count_0']].dropna(axis=0, how='all')
        first_50.columns = ['Name', 'Count']

        second_50 = df[['Name_1', 'Count_1']].dropna(axis=0, how='all')
        second_50.columns = ['Name', 'Count']

        full = first_50.append(second_50)
        full['Year'] = year

        full['Country'] = country
        full['Gender'] = gender

        return full

    except:
        print('Country parser failed for {0} names in {1}'.format(gender, year))
        # print(df)
        raise
