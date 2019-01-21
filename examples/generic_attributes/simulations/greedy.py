from declarations import FieldAllocation
from declarations import FieldState
from declarations import OperationResult


class GreedyAllocation(FieldAllocation):

    def get_name(self):
        return "Gre"

    def get_alternate_field(self, all_fields, reallocated_field):
        fields = []

        remaining_tags = reallocated_field.num_tags
        finding_failed = False

        for field in all_fields:

            if field.field_state is not FieldState.OTHER:
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
        abort = OperationResult.ALLOC_FAILED

        for field in fields:
            if field.field_state is not FieldState.OTHER:
                if field.num_tags < field.max_tags:
                    field.field_state = FieldState.TAGGING
                    abort = OperationResult.NO_ERROR
                    field.num_tags += 1
                    break

        return abort
