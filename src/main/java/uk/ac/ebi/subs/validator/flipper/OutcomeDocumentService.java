package uk.ac.ebi.subs.validator.flipper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ebi.subs.data.component.Archive;
import uk.ac.ebi.subs.validator.data.ValidationOutcome;
import uk.ac.ebi.subs.validator.data.ValidationOutcomeEnum;
import uk.ac.ebi.subs.validator.repository.ValidationOutcomeRepository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This is a service to modify the {@code ValidationOutcome} status according to the entities validation outcome.
 */
@Service
public class OutcomeDocumentService {

    public static final Logger logger = LoggerFactory.getLogger(OutcomeDocumentService.class);

    @Autowired
    private ValidationOutcomeRepository repository;

    public boolean updateValidationOutcome(String uuid) {
        ValidationOutcome validationOutcome = repository.findOne(uuid);

        if (validationOutcome != null) { // The queried document already has been deleted.
            if (isLatestVersion(validationOutcome.getSubmissionId(), validationOutcome.getEntityUuid(), validationOutcome.getVersion())) {
                flipStatusIfRequired(validationOutcome, uuid);

                return true;
            }
        }

        return false;
    }

    private boolean isLatestVersion(String submissionId, String entityUuid, int thisOutcomeVersion) {
        List<ValidationOutcome> validationOutcomes = repository.findBySubmissionIdAndEntityUuid(submissionId, entityUuid);

        if (validationOutcomes.size() > 0) {
            List<Integer> versions = validationOutcomes.stream()
                    .map(validationOutcome -> validationOutcome.getVersion())
                    .collect(Collectors.toList());

            int max = Integer.valueOf(Collections.max(versions));
            if (max > Integer.valueOf(thisOutcomeVersion)) {
                return false;
            }
        }
        return true;
    }

    private void flipStatusIfRequired(ValidationOutcome validationOutcome, String uuid) {
        Map<Archive, Boolean> validationResults = validationOutcome.getExpectedOutcomes();
        if (!validationResults.values().contains(false)){
            validationOutcome.setValidationOutcome(ValidationOutcomeEnum.Complete);
            repository.save(validationOutcome);

            logger.info("Validation outcome document with id {} is completed.", uuid);
        }
    }
}
