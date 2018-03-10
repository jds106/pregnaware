import pandas as pd
import os.path
import os

import mysql.connector

import namestatsparsers.monthparser as monthparser
import namestatsparsers.countryparser as countryparser
import namestatsparsers.regionparser as regionparser
import namestatsparsers.completelistparser as completelistparser


# Writes the files to the database
def save_to_database(df: pd.DataFrame, table_name: str):
    con = mysql.connector.connect(
            host='ec2-34-245-208-245.eu-west-1.compute.amazonaws.com',
            database='pregnaware',
            user='pregnaware',
            password=os.environ['DB_PREGNAWARE_PWD'])

    df.to_sql(con=con, name=table_name, flavor='mysql', if_exists='append', index=False)


# Parses the data from the specified Excel workbook
def parse(filepath: str, filename: str):
    tokens = filename.split('.')
    year = int(tokens[1])
    gender = tokens[2].lower()

    print('Parsing file: {0}'.format(filename))
    df_dict = pd.read_excel('{0}/{1}'.format(filepath, filename), sheetname=None)
    assert(isinstance(df_dict, dict))

    # Top 100 names ranked by country
    key_top_100_ew = None
    key_top_100_e = None
    key_top_100_w = None
    key_top_10_region = None
    key_top_10_month = None
    key_full = None

    for key in df_dict.keys():
        assert(isinstance(key, str))
        if key.lower().endswith('by region'):
            key_top_10_region = key

        elif key.lower().endswith('by month'):
            key_top_10_month = key

        # Variants for England & Wales
        elif key.lower().endswith('top 100 {0}, e&w'.format(gender)):
            key_top_100_ew = key
        elif key.lower().endswith('top 100 {0}\' names'.format(gender)):
            key_top_100_ew = key

        # Variants for England
        elif key.lower().endswith('top 100 {0}, england'.format(gender)):
            key_top_100_e = key
        elif key.lower().endswith('top 100 {0}, eng'.format(gender)):
            key_top_100_e = key

        elif key.lower().endswith('top 100 {0}, wales'.format(gender)):
            key_top_100_w = key

        elif key.lower().endswith('{0} names - e&w'.format(gender)):
            key_full = key

        elif key not in ['Contents', 'Metadata', 'Terms and Conditions', 'Related Publications']:
            print('Unknown table: {0}'.format(key))

    if key_top_100_ew:
        results = countryparser.parse(year, gender, 'England and Wales', df_dict[key_top_100_ew])
        save_to_database(results, 'NameStatByCountry')

    if key_top_100_e:
        results = countryparser.parse(year, gender, 'England', df_dict[key_top_100_e])
        save_to_database(results, 'NameStatByCountry')

    if key_top_100_w:
        results = countryparser.parse(year, gender, 'Wales', df_dict[key_top_100_w])
        save_to_database(results, 'NameStatByCountry')

    if key_top_10_region:
        results = regionparser.parse(year, gender, df_dict[key_top_10_region])
        save_to_database(results, 'NameStatByRegion')

    if key_top_10_month:
        results = monthparser.parse(year, gender, df_dict[key_top_10_month])
        # print(results)
        save_to_database(results, 'NameStatByMonth')

    if key_full:
        results = completelistparser.parse(year, gender, df_dict[key_full])
        save_to_database(results, 'NameStat')

path = '../../data/ons_babynames'
for file in os.listdir(path):
    if file.startswith('babynames.') and file.endswith('.xls'):
        parse(path, file)
