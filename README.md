# Validation Status Flipper
[![Build Status](https://travis-ci.org/EMBL-EBI-SUBS/validator-status-flipper.svg?branch=master)](https://travis-ci.org/EMBL-EBI-SUBS/validator-status-flipper)

This service is listening on events on the validation aggregation results Queue.
When processing a published event it will update the ValidationResult document's status according to the availability of the validation results. If all the entity has been validated, then the status will change to Complete, otherwise it will stay Pending as initially.

## License
This project is licensed under the Apache 2.0 License - see the [LICENSE.md](LICENSE.md) file for details
