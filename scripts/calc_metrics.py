from __future__ import print_function
import sys


def print_metric(name, metric, output_file):
    print(name+":", round(metric*100, 2), "%", file=output_file)


def calc_metrics(results_file):

    path, extension = results_file.split('.')

    with open(results_file, 'r') as results, \
            open(path + '_metrics.' + extension, 'w+') as metrics:
    
        for res_line in results:
            if res_line == "=== Confusion Matrix ===\n":

                # skip irrelevant lines
                next(results)
                next(results)

                sim_line = next(results)
                tp, fn = map(float, sim_line.split()[:2])

                diff_line = next(results)
                fp, tn = map(float, diff_line.split()[:2])

                a = (tp + tn)/(tp + fn + fp + tn)
                p = tp/(tp + fp)
                r = tp/(tp + fn)
                f = 2 * p * r/(p + r)

                print_metric('A', a, metrics)
                print_metric('P', p, metrics)
                print_metric('R', r, metrics)
                print_metric('F', f, metrics)
                break

if __name__ == "__main__":
    calc_metrics(sys.argv[1])
