import urllib.request
import urllib.error
import http.client
import os.path


# Download the file, and write it to the directory specified (returns the file name)
def download(year: int, gender: str, url: str, tgt_dir: str) -> str:

    local_filename = "{0}/babynames.{1}.{2}.xls".format(tgt_dir, year, gender)
    if os.path.isfile(local_filename):
        print('Skipping current file: {0}'.format(local_filename))
        return

    print('Fetching {0} names for {1} from {2}'.format(gender, year, url))

    req = urllib.request.Request(
            url,
            data=None,
            headers={
                'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.47 Safari/537.36'
            })

    try:
        with urllib.request.urlopen(req) as response:
            assert(isinstance(response, http.client.HTTPResponse))
            x = response.read()
            f = open(local_filename, "wb")
            f.write(x)
            f.close()

    except urllib.error.HTTPError as err:
        print('Failed to fetch {0} names for {1} from {2}: {3}'.format(gender, year, url, err))


def download_all():
    # Based on data from this website:
    #   ROOT:  http://www.ons.gov.uk/peoplepopulationandcommunity/birthsdeathsandmarriages/livebirths/datasets/
    #   Girls: ROOT / babynamesenglandandwalesbabynamesstatisticsgirls
    #   Boys:  ROOT / babynamesenglandandwalesbabynamesstatisticsboys

    root = 'http://www.ons.gov.uk/file?uri=/peoplepopulationandcommunity/birthsdeathsandmarriages/livebirths/datasets'
    boys_url_root = root + '/babynamesenglandandwalesbabynamesstatisticsboys'
    girls_url_root = root + '/babynamesenglandandwalesbabynamesstatisticsgirls'

    urls = {
        'boys': {
            2014: boys_url_root + '/2014/2014boysbyareagorrsmonthwebtables_tcm77-413738.xls',
            2013: boys_url_root + '/2013/2013boysbyareagorrsmonthwebtables_tcm77-374580.xls',
            2012: boys_url_root + '/2012/2012boysbyareagorrsmonthwebtables_tcm77-323077.xls',
            2011: boys_url_root + '/2011/2011boysbabynamesfinal_tcm77-276133.xls',
            2010: boys_url_root + '/2010/2010boys_tcm77-253928.xls',
            2009: boys_url_root + '/2009/2009boys_tcm77-253932.xls',
            2008: boys_url_root + '/2008/2008boys_tcm77-253966.xls',
            2007: boys_url_root + '/2007/2007boys_tcm77-253973.xls',
            2006: boys_url_root + '/2006/2006boys_tcm77-253978.xls',
            2005: boys_url_root + '/2005/2005boys_tcm77-253982.xls',
            2004: boys_url_root + '/2004/2004boys_tcm77-253986.xls',
            2003: boys_url_root + '/2003/2003boys_tcm77-253990.xls',
            2002: boys_url_root + '/2002/2002boys_tcm77-253994.xls',
            2001: boys_url_root + '/2001/2001boys_tcm77-254000.xls',
            2000: boys_url_root + '/2000/2000boys_tcm77-254008.xls',
            1999: boys_url_root + '/1999/1999boys_tcm77-254014.xls',
            1998: boys_url_root + '/1998/1998boys_tcm77-254018.xls',
            1997: boys_url_root + '/1997/1997boys_tcm77-254022.xls',
            1996: boys_url_root + '/1996/1996boys_tcm77-254026.xls',

        },
        'girls': {
            2014: girls_url_root + '/2014/2014girlsbyareagorrsmonthwebtables_tcm77-413741.xls',
            2013: girls_url_root + '/2013/2013girlsbyareagorrsmonthwebtables_tcm77-374588.xls',
            2012: girls_url_root + '/2012/2012girlsbyareagorrsmonthwebtables_tcm77-323080.xls',
            2011: girls_url_root + '/2011/2011girlsbabynamesfinal_tcm77-276135.xls',
            2010: girls_url_root + '/2010/2010girls_tcm77-253930.xls',
            2009: girls_url_root + '/2009/2009girls_tcm77-253940.xls',
            2008: girls_url_root + '/2008/2008girls_tcm77-253964.xls',
            2007: girls_url_root + '/2007/2007girls_tcm77-253971.xls',
            2006: girls_url_root + '/2006/2006girls_tcm77-253976.xls',
            2005: girls_url_root + '/2005/2005girls_tcm77-253980.xls',
            2004: girls_url_root + '/2004/2004girls_tcm77-253984.xls',
            2003: girls_url_root + '/2003/2003girls_tcm77-253988.xls',
            2002: girls_url_root + '/2002/2002girls_tcm77-253992.xls',
            2001: girls_url_root + '/2001/2001girls_tcm77-253998.xls',
            2000: girls_url_root + '/2000/2000girls_tcm77-254006.xls',
            1999: girls_url_root + '/1999/1999girls_tcm77-254010.xls',
            1998: girls_url_root + '/1998/1998girls_tcm77-254016.xls',
            1997: girls_url_root + '/1997/1997girls_tcm77-254020.xls',
            1996: girls_url_root + '/1996/1996girls_tcm77-254024.xls',
        }
    }

    for gender in urls.keys():
        for year in urls[gender].keys():
            download(int(year), gender, str(urls[gender][year]), '/Users/james/tmp/babynames')

download_all()
