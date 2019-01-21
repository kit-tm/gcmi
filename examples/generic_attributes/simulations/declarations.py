from enum import Enum
import math
from functools import reduce
import numpy as np
import os
import csv

test_id = 'test_name'


class MethodContainer:
    def __init__(self, allocation_method):
        self.errors = {}

        self.num_conflicts = []
        self.severities = []
        self.total_tags_moved = []

        self.allocation_method = allocation_method
        self.errors[OperationResult.ALLOC_FAILED] = 0
        self.errors[OperationResult.RE_ALLOC_FAILED] = 0

    def new_data(self, sev, res):
        if res is OperationResult.NO_ERROR:
            self.num_conflicts.append(len(sev))
            self.severities.extend(sev)

            if len(sev) > 0:
                self.total_tags_moved.append(reduce((lambda x, y: x + y), sev))
            else:
                self.total_tags_moved.append(0)
        else:
            self.errors[res] += 1

    def print_results(self, ratio, msg_num, iter):
        avg_num_conflicts = -1
        min_num_conflicts = -1
        max_num_conflicts = -1

        if len(self.num_conflicts) > 0:
            avg_num_conflicts = np.array(self.num_conflicts).mean()
            min_num_conflicts = avg_num_conflicts - np.std(np.array(self.num_conflicts))
            max_num_conflicts = avg_num_conflicts + np.std(np.array(self.num_conflicts))

        avg_severities = -1
        min_severities = -1
        max_severities = -1

        if len(self.severities) > 0:
            avg_severities = np.array(self.severities).mean()
            min_severities = avg_severities - np.std(np.array(self.severities))
            max_severities = np.std(np.array(self.severities)) + avg_severities

        tags_moved = -1
        min_moved_tags = -1
        max_moved_tags = -1

        if len(self.total_tags_moved) > 0:
            tags_moved = np.array(self.total_tags_moved).mean()
            min_moved_tags = tags_moved - np.std(np.array(self.total_tags_moved))
            max_moved_tags = tags_moved + np.std(np.array(self.total_tags_moved))

        folder_name = "/home/david/Documents/ma-koerver-thesis/thesis/eval/{}".format(test_id)

        if not os.path.exists(folder_name):
            os.makedirs(folder_name)

        name = folder_name + '/{}.csv'.format(self.allocation_method.get_name())
        try:
            file = open(name, 'r')
        except FileNotFoundError:
            with open(name, 'a') as file:
                writer = csv.writer(file)
                writer.writerow(['iter','ratio', 'num_msg', 'min_sev', 'avg_sev', 'max_sev',
                                 'min_conf', 'avg_conf', 'max_conf',
                                 'min_moved', 'avg_moved', 'max_moved',
                                 "alloc_failed", "realloc_failed"])

        with open(name, 'a') as file:
            writer = csv.writer(file)
            writer.writerow([iter, round(ratio, 4), msg_num,
                    round(min_severities, 4), round(avg_severities, 4), round(max_severities, 4),
                    round(min_num_conflicts, 4), round(avg_num_conflicts, 4), round(max_num_conflicts, 4),
                    round(min_moved_tags, 4), round(tags_moved, 4), round(max_moved_tags, 4),
                    self.errors[OperationResult.ALLOC_FAILED], self.errors[OperationResult.RE_ALLOC_FAILED]])

        '''
        For debugging purposes
        
        print("##########################################################################")
        print("Current Allocation method is {}".format(self.allocation_method.get_name()))
        print("Number message_store is {}".format(msg_num))
        print("Allocations failed {} times.".format(self.errors[OperationResult.ALLOC_FAILED]))
        print("Reallocations failed {} times.".format(self.errors[OperationResult.RE_ALLOC_FAILED]))
        print("Conflicts occured {} times in average.".format(avg_num_conflicts))
        #print("Std of occured conflict number is {}.".format(std_num_conflicts))
        print("Reallocation severity is {} in average.".format(avg_severities))
        #print("Std of severities is {}.".format(std_severities))
        print("Total number tags moved is {} in average.".format(tags_moved))
        #print("Std of moved tags is {}.".format(std_moved_tags))
        print("##########################################################################")'''


def num_tags(bits):
    return int(math.pow(2, bits))


def print_fields(all_fields):
    print("++++++++++++++ Printing all fields ++++++++++++++")

    for field in all_fields:
        print("Field State= {}; Number Tags: {}; Max Tags: {}; Probability: {}".format(field.field_state, field.num_tags, field.max_tags, field.probability))

    print("++++++++++++ End Printing all fields ++++++++++++")


class FieldAllocation:
    pass


class OperationResult(Enum):
    NO_ERROR = 1
    ALLOC_FAILED = 2
    RE_ALLOC_FAILED = 3


class TagAllocationMethod(Enum):
    GREEDY = 1
    PROBABILISTIC = 2


class FieldState(Enum):
    NO_USE = 1
    TAGGING = 2
    OTHER = 3


class Field:
    def __init__(self, probability, max_bits):
        self.field_state = FieldState.NO_USE
        self.num_tags = 0
        self.probability = probability
        self.max_tags = num_tags(max_bits)


REALLOCATION_TIME_METHOD = "lin"

DIRECT_FIELD_SIZE = [48, 48, 8, 8]
DIRECT_FIELD_PROB = [0.003, 0.01, 0.002, 0.005]


def reallocation_overhead(number_tags):
    if REALLOCATION_TIME_METHOD is "exp":
        return math.pow(2, number_tags)
    else:
        return number_tags


MAX_SEVERITY = reallocation_overhead(num_tags(max(DIRECT_FIELD_SIZE)))
MAX_FIELD_SIZE = 8
NUM_FIELDS = 10
TAG_REGULAR_RATIO = 0.9
NUM_FLOW_MOD_MSG = 1000
NUM_RUNS = 1000


def reduce_ratio(reduction):
    global TAG_REGULAR_RATIO
    TAG_REGULAR_RATIO -= reduction


def reduce_msg(reduction):
    global NUM_FLOW_MOD_MSG
    NUM_FLOW_MOD_MSG = NUM_FLOW_MOD_MSG * reduction
