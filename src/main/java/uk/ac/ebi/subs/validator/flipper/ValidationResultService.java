package uk.ac.ebi.subs.validator.flipper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;
import uk.ac.ebi.subs.validator.data.ValidationAuthor;
import uk.ac.ebi.subs.validator.data.ValidationResult;
import uk.ac.ebi.subs.validator.data.ValidationStatus;
import uk.ac.ebi.subs.validator.repository.ValidationResultRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This is a service to modify the {@code ValidationResult} status according to the entities validation result.
 */
@Service
public class ValidationResultService {

    public static final Logger logger = LoggerFactory.getLogger(ValidationResultService.class);

    @Autowired
    private ValidationResultRepository repository;

    public boolean updateValidationResult(String uuid) {
        ValidationResult validationResult = repository.findOne(uuid);

        if (validationResult != null) {
            if (isLatestVersion(validationResult.getEntityUuid(), validationResult.getVersion())) {
                flipStatusIfRequired(validationResult, uuid);
                return true;
            }
        }
        return false;
    }

    private boolean isLatestVersion(String entityUuid, int thisValidationResultVersion) {
        ValidationResult persistedValidationResult = repository.findByEntityUuid(entityUuid);

        if (persistedValidationResult != null) {
            if (persistedValidationResult.getVersion() > thisValidationResultVersion) {
                return false;
            }
        }
        return true;
    }

    private void flipStatusIfRequired(ValidationResult validationResult, String uuid) {
        Map<ValidationAuthor, List<SingleValidationResult>> validationResults = validationResult.getExpectedResults();

        if (validationResults.values().stream().filter(list -> list.isEmpty()).collect(Collectors.toList()).isEmpty()) {
            validationResult.setValidationStatus(ValidationStatus.Complete);
            repository.save(validationResult);

            logger.info("Validation result document with id {} is completed.", uuid);
        }
    }

}
