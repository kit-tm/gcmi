from declarations import FieldAllocation
from declarations import FieldState
from declarations import OperationResult
from declarations import MAX_SEVERITY
from declarations import reallocation_overhead


class ProbabilisticAllocation(FieldAllocation):

    def __init__(self, weight):
        self.tag_weight = weight

    def get_name(self):
        return "Op{}".format(self.tag_weight)

    def get_cost(self, field):
        return self.tag_weight * reallocation_overhead(field.num_tags) / MAX_SEVERITY

    def sort_fields(self, all_fields):
        return sorted(all_fields, key=lambda element: 10 * element.field_state.value + element.probability + self.get_cost(element))

    def get_alternate_field(self, all_fields, reallocated_field):
        fields = []

        remaining_tags = reallocated_field.num_tags
        finding_failed = False

        sorted_fields = self.sort_fields(all_fields)

        for field in sorted_fields:

            if field.field_state is FieldState.OTHER:
                finding_failed = True
                break

            available = field.max_tags - field.num_tags

            if available > 0:
                remaining_tags -= available
                fields.append(field)

                if remaining_tags <= 0:
                    break

        if remaining_tags > 0:
            finding_failed = True

        return finding_failed, fields

    def alloc(self, fields):
        abort = OperationResult.NO_ERROR

        sorted_fields = self.sort_fields(fields)

        for field in sorted_fields:
            if field.field_state is FieldState.OTHER:
                abort = OperationResult.ALLOC_FAILED
                break
            else:
                if field.num_tags < field.max_tags:
                    field.field_state = FieldState.TAGGING
                    field.num_tags += 1
                    break

        return abort
