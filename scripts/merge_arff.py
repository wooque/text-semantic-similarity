from __future__ import print_function
import sys


def parse_arff(filename):

    arff = {'relation': '', 'attrs': [], 'data': []}

    with open(filename, 'r') as arff_file:

        for line in arff_file:

            if line.startswith("@RELATION"):
                arff['relation'] = line.strip().split()[1]

            elif line.startswith("@ATTRIBUTE"):
                parts = line.strip().split()
                arff['attrs'].append({'name': parts[1], 'data': parts[2]})

            elif line.startswith("@DATA"):
                for data_line in arff_file:
                    arff['data'].append(data_line.strip().split(','))

    return arff


def write_arff(arff, filename):

    with open(filename, 'w+') as arff_file:

        print("@RELATION", arff['relation'], file=arff_file)
        print("", file=arff_file)

        for attr in arff['attrs']:
            print("@ATTRIBUTE", attr['name'], attr['data'], file=arff_file)

        print("", file=arff_file)
        print("@DATA", file=arff_file)

        for data in arff['data']:
            print(",".join(data), file=arff_file)


def merge_arff(first, second, merged):

    first_arff = parse_arff(first)
    second_arff = parse_arff(second)

    merged_arff = {'relation': first_arff['relation'],
                   'attrs': second_arff['attrs'][:-1] + first_arff['attrs'],
                   'data': []}

    for (first_data, second_data) in zip(first_arff['data'], second_arff['data']):
        merged_arff['data'].append(second_data[:-1] + first_data)

    write_arff(merged_arff, merged)


if __name__ == "__main__":
    core = sys.argv[1]
    additional = sys.argv[2]
    new = sys.argv[3]
    merge_arff(core, additional, new)