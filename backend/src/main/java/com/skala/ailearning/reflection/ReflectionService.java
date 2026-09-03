package com.skala.ailearning.reflection;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skala.ailearning.common.NotFoundException;
import com.skala.ailearning.course.Lecture;
import com.skala.ailearning.course.LectureRepository;
import com.skala.ailearning.user.User;
import com.skala.ailearning.user.UserRepository;

@Service
@Transactional(readOnly = true)
public class ReflectionService {

    private final ReflectionRepository reflectionRepository;
    private final UserRepository userRepository;
    private final LectureRepository lectureRepository;

    public ReflectionService(ReflectionRepository reflectionRepository,
                             UserRepository userRepository,
                             LectureRepository lectureRepository) {
        this.reflectionRepository = reflectionRepository;
        this.userRepository = userRepository;
        this.lectureRepository = lectureRepository;
    }

    @Transactional
    public ReflectionResponse create(ReflectionRequest request) {
        User learner = userRepository.findById(request.userId())
                .orElseThrow(() -> new NotFoundException("학습자를 찾을 수 없습니다."));
        Lecture lecture = lectureRepository.findById(request.lectureId())
                .orElseThrow(() -> new NotFoundException("강의를 찾을 수 없습니다."));

        Reflection reflection = new Reflection(
                learner,
                lecture,
                request.understood().trim(),
                request.difficult().trim(),
                normalize(request.wantsToLearn()));

        return ReflectionResponse.from(reflectionRepository.save(reflection));
    }

    public Reflection getEntity(Long reflectionId) {
        return reflectionRepository.findById(reflectionId)
                .orElseThrow(() -> new NotFoundException("회고록을 찾을 수 없습니다."));
    }

    public ReflectionResponse findOne(Long reflectionId) {
        return ReflectionResponse.from(getEntity(reflectionId));
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }
}
