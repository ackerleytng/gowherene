#!/usr/bin/env python

import os
import sys
import argparse
from datetime import datetime
from urllib import (request, parse)


HEADER = 'filename,source,datetime'


def scrape(url, output_path):
    req = request.Request(
        url, 
        data=None, 
        headers={
            'User-Agent': 'getter/1.0'
        }
    )
    with open(output_path, 'wb') as f:
        f.write(request.urlopen(req).read())


def get_suitable_save_filename(url):
    web_path = parse.urlparse(url).path.rstrip('/')
    filename = os.path.basename(web_path)
    filename = filename if filename.endswith('.html') else filename + '.html'
    return filename


def has_correct_format(record_fname):
    with open(record_fname) as f:
        contents = f.read()

    if (len(contents) > 0 and
        (not contents.startswith(HEADER) or
         not contents.endswith('\n'))):
        return False

    return True



def append_to_record(record_fname, url, out_fname):
    # Create file if necessary
    if os.path.exists(record_fname):
        if not has_correct_format(record_fname):
            print('Records should have the header "{}"'.format(HEADER))
            return 1
    else:
        with open(record_fname, 'w') as f:
            f.write(HEADER + '\n')
    
    with open(record_fname, 'a') as f:
        f.write('{},{},{}\n'.format(out_fname, url, datetime.now()))


def main(args):
    if args.output_path:
        out_fname = args.output_path
    else:
        out_fname = get_suitable_save_filename(args.url)

    out_fpath = os.path.join('files', out_fname)
    if os.path.exists(out_fpath):
        print('{} already exists. Specify another?'.format(out_fname))
        return 1

    print('Downloading {} to {}...'.format(args.url, out_fpath))
    scrape(args.url, out_fpath)

    return append_to_record(args.record_file, args.url, out_fname)


if __name__ == '__main__':
    parser = argparse.ArgumentParser(
        description=('Scrape one site, write to this directory, '
                     'and write a record to record file'))
    parser.add_argument('url')
    parser.add_argument('-o', '--output_path',
                        help='a specific output path')
    parser.add_argument('-f', '--record_file',
                        help='the record csv to append to',
                        default='records.csv')
    sys.exit(main(parser.parse_args()))

