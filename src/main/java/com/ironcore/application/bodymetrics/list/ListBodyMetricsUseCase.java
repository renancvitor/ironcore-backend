package com.ironcore.application.bodymetrics.list;

import com.ironcore.application.exception.ResourceNotFoundException;
import com.ironcore.application.exception.UserInactiveException;
import com.ironcore.application.shared.pagination.PageQuery;
import com.ironcore.application.shared.pagination.PageResult;
import com.ironcore.application.bodymetrics.port.ListBodyMetricsQueryPort;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListBodyMetricsUseCase {

    private final UserRepository userRepository;
    private final ListBodyMetricsQueryPort queryPort;

    @Transactional(readOnly = true)
    public ListBodyMetricsResult execute(ListBodyMetricsCommand command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        if (!user.isActive()) {
            throw new UserInactiveException("Usuário inativo.");
        }

        PageQuery pageQuery = new PageQuery(
                command.page(),
                command.size()
        );

        PageResult<ListBodyMetricsItemResult> metrics = queryPort
                .findByUserIdOrderByMeasuredAtDesc(
                        command.userId(),
                        pageQuery
                );

        return new ListBodyMetricsResult(metrics);
    }
}
