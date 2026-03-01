package com.eligibility.presentation.mapper;

import com.eligibility.application.dto.request.AddCriteriaRequest;
import com.eligibility.application.dto.request.CreateLotteryRequest;
import com.eligibility.application.dto.response.LotteryResponse;
import com.eligibility.application.port.in.AddLotteryCriteriaUseCase.AddCriteriaCommand;
import com.eligibility.application.port.in.CreateLotteryUseCase.CreateLotteryCommand;
import com.eligibility.domain.model.Lottery;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Maps between Lottery domain model and HTTP DTOs.
 */
@Component
public class LotteryMapper {

    public CreateLotteryCommand toCommand(CreateLotteryRequest request) {
        return new CreateLotteryCommand(request.name());
    }

    /**
     * Maps the nested criteria entries from the request into commands.
     * Each entry in the batch becomes one AddCriteriaCommand.
     */
    public List<AddCriteriaCommand> toCriteriaCommands(AddCriteriaRequest request) {
        List<AddCriteriaCommand> commands = new ArrayList<>();
        for (AddCriteriaRequest.CriteriaEntry entry : request.criteria()) {
            commands.add(new AddCriteriaCommand(entry.criteriaType(), entry.criteriaValue()));
        }
        return commands;
    }

    public LotteryResponse toResponse(Lottery lottery) {
        return new LotteryResponse(
                lottery.id(),
                lottery.name(),
                lottery.status().name(),
                lottery.createdDate()
        );
    }

    public List<LotteryResponse> toResponseList(List<Lottery> lotteries) {
        List<LotteryResponse> responses = new ArrayList<>();
        for (Lottery lottery : lotteries) {
            responses.add(toResponse(lottery));
        }
        return responses;
    }
}