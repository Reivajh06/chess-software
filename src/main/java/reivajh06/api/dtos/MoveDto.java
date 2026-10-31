package reivajh06.api.dtos;

import java.util.List;
import java.util.Map;

public record MoveDto(int moveId, List<Map<String, String>> tiles) {}
