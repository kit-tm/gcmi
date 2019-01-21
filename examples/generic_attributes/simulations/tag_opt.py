import random as rand
from declarations import FieldState
from declarations import Field
from declarations import OperationResult
from probabilistic import ProbabilisticAllocation
from declarations import reallocation_overhead
from declarations import NUM_FIELDS
from declarations import MAX_FIELD_SIZE
from declarations import DIRECT_FIELD_SIZE
from declarations import DIRECT_FIELD_PROB
from declarations import NUM_FLOW_MOD_MSG
from declarations import NUM_RUNS
from declarations import TAG_REGULAR_RATIO
from greedy import GreedyAllocation
from declarations import MethodContainer
from functools import reduce


all_fields = []
chosen_fields = []
field_probs = []
iter = 0

'''
Setting up probabilities and fields
'''
def init_fields():
    all_fields.clear()
    field_probs.clear()

    if DIRECT_FIELD_SIZE is None:
        max_field_size = 0

        for i in range(0, NUM_FIELDS):
            field_size = int(rand.random() * (MAX_FIELD_SIZE + 1))
            prob = 1 / NUM_FIELDS

            all_fields.append(Field(1/NUM_FIELDS, field_size))
            field_probs.append(prob)

            if field_size > max_field_size:
                max_field_size = field_size

        global MAX_SEVERITY
        MAX_SEVERITY = reallocation_overhead(max_field_size)
    else:
        for i in range(0, len(DIRECT_FIELD_SIZE)):
            field_probs.append(DIRECT_FIELD_PROB[i])
            all_fields.append(Field(DIRECT_FIELD_PROB[i], DIRECT_FIELD_SIZE[i]))

'''
Determining field selections to simulate one run
'''
def init_field_selections(ratio, msg_num):
    values = []
    chosen_fields.clear()

    for i in range(0, len(DIRECT_FIELD_SIZE) + 1):
        values.append(i)

    remain_prop = 1 - reduce((lambda x, y: x + y), DIRECT_FIELD_PROB)

    prop_weights = DIRECT_FIELD_PROB + [remain_prop]

    for i in range(0, msg_num):
        if rand.random() < ratio:
            chosen_fields.append(-1)
        else:
            chosen_fields.append(rand.choices(values, weights=prop_weights)[0])

'''
One iteration for a strategy, i.e. alloc method
'''
def iteration(alloc_method):

    severity = []
    init_fields()

    result = OperationResult.NO_ERROR

    for i in range(0, len(chosen_fields)):
        if chosen_fields[i] < 0:
            result = alloc_method.alloc(all_fields)
        elif chosen_fields[i] == len(DIRECT_FIELD_PROB):
            continue
        else:
            # (Field, Reallocation Required)
            reallocation_required = False

            field = all_fields[chosen_fields[i]]

            if field.field_state is FieldState.NO_USE:
                reallocation_required = False

            # conflict happened
            elif field.field_state is FieldState.TAGGING:
                reallocation_required = True

            field.field_state = FieldState.OTHER

            if reallocation_required:
                severity.append(reallocation_overhead(field.num_tags))

                (finding_failed, selected_fields) = alloc_method.get_alternate_field(all_fields, field)

                if finding_failed:
                    result = OperationResult.RE_ALLOC_FAILED
                    break

                for reallocation_field in selected_fields:

                    remaining = reallocation_field.max_tags - reallocation_field.num_tags

                    if remaining < field.num_tags:
                        reallocation_field.num_tags = reallocation_field.max_tags
                    else:
                        reallocation_field.num_tags += field.num_tags

                    reallocation_field.field_state = FieldState.TAGGING

        if result is not OperationResult.NO_ERROR:
            break

    return severity, result


'''
Runs the simulation with given alloc methods to test
'''
def run(methods, ratio, msg_num):
    for i in range(0, NUM_RUNS):
        init_field_selections(ratio, msg_num)

        for method in methods:
            sev, res = iteration(method.allocation_method)
            method.new_data(sev, res)


    for method in methods:
       method.print_results(ratio, msg_num, iter)


if __name__ == "__main__":
    ratio = TAG_REGULAR_RATIO
    msg_num = NUM_FLOW_MOD_MSG

    for i in range(0, 10):
        testing_methods = []

        global iter
        iter = i

        testing_methods.append(MethodContainer(ProbabilisticAllocation(0.0)))
        testing_methods.append(MethodContainer(ProbabilisticAllocation(0.5)))
        testing_methods.append(MethodContainer(ProbabilisticAllocation(1.0)))
        testing_methods.append(MethodContainer(GreedyAllocation()))

        run(testing_methods, ratio, msg_num)

        # variable that is reduced for subsequent runs...
        ratio = ratio - 0.1
        #msg_num += 1000
        #reduce_msg(0.4)


