from __future__ import print_function
import sys


def clean_data(filename):

    with open(filename, 'r') as in_file,\
            open('clean_' + filename, 'w+') as out_file:

        # skip first line, header
        next(in_file)

        for in_line in in_file:
            parts = in_line.split('\t')

            # save relevant data: similarity, first and second sentence
            print(parts[0], file=out_file)
            print(parts[3], file=out_file)
            print(parts[4], file=out_file, end='')

if __name__ == "__main__":
    clean_data(sys.argv[1])