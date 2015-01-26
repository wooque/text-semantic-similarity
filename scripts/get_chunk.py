from __future__ import print_function
import sys


def chunk(filename, num):

    path, extension = filename.split('.')

    with open(filename, 'r') as in_file,\
            open(path + '_' + str(num) + '.' + extension, 'w+') as out_file:

        # sentences data are in 3 lines
        for i in xrange(num*3):
            print(in_file.readline(), file=out_file, end='')

if __name__ == '__main__':
    sentences_file = sys.argv[1]
    num_of_lines = int(sys.argv[2])
    chunk(sentences_file, num_of_lines)