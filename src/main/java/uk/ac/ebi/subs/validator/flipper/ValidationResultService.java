package uk.ac.ebi.subs.validator.flipper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ebi.subs.data.component.Archive;
import uk.ac.ebi.subs.validator.data.ValidationResult;
import uk.ac.ebi.subs.validator.data.ValidationStatus;
import uk.ac.ebi.subs.validator.repository.ValidationResultRepository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This is a service to modify the {@code ValidationResult} status according to the entities validation outcome.
 */
@Service
public class ValidationResultService {

    public static final Logger logger = LoggerFactory.getLogger(ValidationResultService.class);

    @Autowired
    private ValidationResultRepository repository;

    public boolean updateValidationResult(String uuid) {
        ValidationResult validationResult = repository.findOne(uuid);

        if (validationResult != null) { // The queried document already has been deleted.
            if (isLatestVersion(validationResult.getSubmissionId(), validationResult.getEntityUuid(), validationResult.getVersion())) {
                flipStatusIfRequired(validationResult, uuid);

                return true;
            }
        }

        return false;
    }

    private boolean isLatestVersion(String submissionId, String entityUuid, int thisValidationResultVersion) {
        List<ValidationResult> validationResults = repository.findBySubmissionIdAndEntityUuid(submissionId, entityUuid);

        if (validationResults.size() > 0) {
            List<Integer> versions = validationResults.stream()
                    .map(validationResult -> validationResult.getVersion())
                    .collect(Collectors.toList());

            int max = Collections.max(versions);
            if (max > thisValidationResultVersion) {
                return false;
            }
        }
        return true;
    }

    private void flipStatusIfRequired(ValidationResult validationResult, String uuid) {
        Map<Archive, Boolean> validationResults = validationResult.getExpectedResults();
        if (!validationResults.values().contains(false)){
            validationResult.setValidationStatus(ValidationStatus.Complete);
            repository.save(validationResult);

            logger.info("Validation result document with id {} is completed.", uuid);
        }
    }
}
