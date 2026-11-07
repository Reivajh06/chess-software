package reivajh06.archchess.infrastructure.adapters.web.dtos;

import java.util.List;

public record PieceMovesResponseDto(
		List<MoveDto> moves
) {
}
