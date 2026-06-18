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

    private static final double SAFE_DISTANCE = 5.0;
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

        CranePosition thisCrane = positionRepository.findByCraneId(request.getCraneId())
                .orElseThrow(() -> new IllegalArgumentException("行车 " + request.getCraneId() + " 不存在"));

        String otherCraneId = "A".equals(request.getCraneId()) ? "B" : "A";
        CranePosition otherCrane = positionRepository.findByCraneId(otherCraneId).orElse(null);

        if (otherCrane != null) {
            double thisStart = thisCrane.getPosition();
            double thisEnd = request.getTargetPosition();
            double otherStart = otherCrane.getPosition();
            double otherEnd = otherCrane.getTargetPosition() != null ? otherCrane.getTargetPosition() : otherCrane.getPosition();

            if (hasCollisionRisk(thisStart, thisEnd, otherStart, otherEnd)) {
                throw new IllegalStateException("指令冲突：有相撞风险！");
            }

            if (Math.abs(thisEnd - otherEnd) < SAFE_DISTANCE) {
                throw new IllegalStateException("指令冲突：两台车终点距离小于安全距离（" + SAFE_DISTANCE + "米）");
            }
        }

        thisCrane.setTargetPosition(request.getTargetPosition());
        thisCrane.setSpeed(CRANE_SPEED);
        thisCrane.setUpdatedAt(LocalDateTime.now());
        positionRepository.save(thisCrane);

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

    private boolean hasCollisionRisk(double aStart, double aEnd, double bStart, double bEnd) {
        if (Math.abs(aStart - bStart) < SAFE_DISTANCE) {
            return true;
        }

        double aMin = Math.min(aStart, aEnd);
        double aMax = Math.max(aStart, aEnd);
        double bMin = Math.min(bStart, bEnd);
        double bMax = Math.max(bStart, bEnd);

        double overlapStart = Math.max(aMin, bMin);
        double overlapEnd = Math.min(aMax, bMax);

        if (overlapStart <= overlapEnd) {
            boolean aStartsLeft = aStart < bStart;
            boolean aEndsLeft = aEnd < bEnd;

            if (aStartsLeft != aEndsLeft) {
                return true;
            }

            double aDir = Math.signum(aEnd - aStart);
            double bDir = Math.signum(bEnd - bStart);

            if (aDir == bDir && aDir != 0) {
                if (aStartsLeft && aEnd > bStart) {
                    return true;
                }
                if (!aStartsLeft && bEnd > aStart) {
                    return true;
                }
            }

            if (overlapEnd - overlapStart < SAFE_DISTANCE) {
                return true;
            }
        }

        double aRange = aMax - aMin;
        double bRange = bMax - bMin;
        double maxRange = Math.max(aRange, bRange);

        if (maxRange > 0) {
            int steps = 100;
            for (int i = 0; i <= steps; i++) {
                double t = (double) i / steps;
                double aPos = aStart + (aEnd - aStart) * t;
                double bPos = bStart + (bEnd - bStart) * t;
                if (Math.abs(aPos - bPos) < SAFE_DISTANCE) {
                    return true;
                }
            }
        }

        return false;
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
                pos.getTargetPosition(),
                pos.getSpeed(),
                pos.getUpdatedAt() != null ? pos.getUpdatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null
        );
    }
}
