package motorph.repository;

import java.util.List;
import motorph.model.AttendanceAdjustmentRequest;

/**
 * AttendanceAdjustmentRepository
 *
 * Repository contract for attendance adjustment requests.
 */
public interface AttendanceAdjustmentRepository {

    // Load all adjustment requests
    List<AttendanceAdjustmentRequest> findAll();

    // Save one new request
    void save(AttendanceAdjustmentRequest request);

    // Rewrite the CSV after approving/rejecting
    void saveAll(List<AttendanceAdjustmentRequest> requests);
}