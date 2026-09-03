package com.g129.ailearning.reflection;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.g129.ailearning.common.NotFoundException;
import com.g129.ailearning.course.Course;
import com.g129.ailearning.course.CourseRepository;
import com.g129.ailearning.user.User;
import com.g129.ailearning.user.UserRepository;

@Service
@Transactional(readOnly = true)
public class ReflectionService {

    private final ReflectionRepository reflectionRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    public ReflectionService(ReflectionRepository reflectionRepository,
                             UserRepository userRepository,
                             CourseRepository courseRepository) {
        this.reflectionRepository = reflectionRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
    }

    @Transactional
    public ReflectionResponse create(ReflectionRequest request) {
        User learner = userRepository.findById(request.userId())
                .orElseThrow(() -> new NotFoundException("학습자를 찾을 수 없습니다."));
        Course course = courseRepository.findById(request.courseId())
                .orElseThrow(() -> new NotFoundException("교육과정을 찾을 수 없습니다."));

        Reflection reflection = new Reflection(
                learner,
                course,
                request.understoodContent().trim(),
                request.difficultContent().trim(),
                normalize(request.questionContent()));

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
