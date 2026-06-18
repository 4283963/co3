package com.crane.dispatch.service;

import com.crane.dispatch.dto.CollisionWarningDto;
import com.crane.dispatch.dto.CranePositionDto;
import com.crane.dispatch.dto.DispatchTaskRequest;
import com.crane.dispatch.entity.CranePosition;
import com.crane.dispatch.entity.CraneTask;
import com.crane.dispatch.repository.CranePositionRepository;
import com.crane.dispatch.repository.CraneTaskRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CraneDispatchService {

    private static final double SAFE_DISTANCE = 10.0;
    private static final double TRACK_LENGTH = 100.0;
    private static final double CRANE_SPEED = 2.0;

    private final CranePositionRepository positionRepository;
    private final CraneTaskRepository taskRepository;

    public CraneDispatchService(CranePositionRepository positionRepository,
                                CraneTaskRepository taskRepository) {
        this.positionRepository = positionRepository;
        this.taskRepository = taskRepository;
    }

    @PostConstruct
    public void init() {
        if (positionRepository.findByCraneId("A").isEmpty()) {
            positionRepository.save(new CranePosition("A", 0.0, 0.0));
        }
        if (positionRepository.findByCraneId("B").isEmpty()) {
            positionRepository.save(new CranePosition("B", 100.0, 0.0));
        }
    }

    public List<CranePositionDto> getAllPositions() {
        return positionRepository.findAll().stream()
                .map(this::toPositionDto)
                .collect(Collectors.toList());
    }

    public CollisionWarningDto checkCollision() {
        CranePosition craneA = positionRepository.findByCraneId("A").orElse(null);
        CranePosition craneB = positionRepository.findByCraneId("B").orElse(null);

        CollisionWarningDto warning = new CollisionWarningDto();

        if (craneA == null || craneB == null) {
            warning.setSafe(true);
            warning.setMessage("行车数据不完整");
            return warning;
        }

        double distance = Math.abs(craneA.getPosition() - craneB.getPosition());
        warning.setCraneAPosition(craneA.getPosition());
        warning.setCraneBPosition(craneB.getPosition());
        warning.setDistance(distance);

        if (distance < SAFE_DISTANCE) {
            warning.setSafe(false);
            warning.setMessage("⚠ 碰撞警告！两台行车距离过近：" + String.format("%.1f", distance) + "米，安全距离为" + SAFE_DISTANCE + "米");
        } else {
            warning.setSafe(true);
            warning.setMessage("安全，当前距离：" + String.format("%.1f", distance) + "米");
        }

        return warning;
    }

    @Transactional
    public CraneTask dispatchTask(DispatchTaskRequest request) {
        if (request.getTargetPosition() < 0 || request.getTargetPosition() > TRACK_LENGTH) {
            throw new IllegalArgumentException("目标位置超出轨道范围(0-" + TRACK_LENGTH + "米)");
        }

        CollisionWarningDto collision = checkCollisionWithTarget(request.getCraneId(), request.getTargetPosition());
        if (!collision.isSafe()) {
            throw new IllegalStateException(collision.getMessage());
        }

        CranePosition crane = positionRepository.findByCraneId(request.getCraneId())
                .orElseThrow(() -> new IllegalArgumentException("行车 " + request.getCraneId() + " 不存在"));

        double distance = Math.abs(request.getTargetPosition() - crane.getPosition());
        double moveTime = distance / CRANE_SPEED;

        crane.setPosition(request.getTargetPosition());
        crane.setSpeed(0.0);
        crane.setUpdatedAt(LocalDateTime.now());
        positionRepository.save(crane);

        CraneTask task = new CraneTask(
                request.getCraneId(),
                request.getTargetPosition(),
                request.getTaskType() != null ? request.getTaskType() : "MOVE",
                request.getDescription()
        );
        task.setStatus("COMPLETED");
        task.setCompletedAt(LocalDateTime.now());
        return taskRepository.save(task);
    }

    private CollisionWarningDto checkCollisionWithTarget(String craneId, double targetPosition) {
        String otherCraneId = "A".equals(craneId) ? "B" : "A";
        CranePosition otherCrane = positionRepository.findByCraneId(otherCraneId).orElse(null);

        CollisionWarningDto warning = new CollisionWarningDto();

        if (otherCrane == null) {
            warning.setSafe(true);
            return warning;
        }

        double distance = Math.abs(targetPosition - otherCrane.getPosition());
        warning.setDistance(distance);

        if ("A".equals(craneId)) {
            warning.setCraneAPosition(targetPosition);
            warning.setCraneBPosition(otherCrane.getPosition());
        } else {
            warning.setCraneAPosition(otherCrane.getPosition());
            warning.setCraneBPosition(targetPosition);
        }

        if (distance < SAFE_DISTANCE) {
            warning.setSafe(false);
            warning.setMessage("⚠ 调度被拒绝！行车" + craneId + "目标位置与行车" + otherCraneId + "距离过近（" + String.format("%.1f", distance) + "米），安全距离为" + SAFE_DISTANCE + "米");
        } else {
            warning.setSafe(true);
        }

        return warning;
    }

    public List<CraneTask> getTaskHistory(String craneId) {
        if (craneId != null && !craneId.isEmpty()) {
            return taskRepository.findByCraneIdOrderByCreatedAtDesc(craneId);
        }
        return taskRepository.findAll();
    }

    private CranePositionDto toPositionDto(CranePosition pos) {
        return new CranePositionDto(
                pos.getCraneId(),
                pos.getPosition(),
                pos.getSpeed(),
                pos.getUpdatedAt() != null ? pos.getUpdatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null
        );
    }
}
