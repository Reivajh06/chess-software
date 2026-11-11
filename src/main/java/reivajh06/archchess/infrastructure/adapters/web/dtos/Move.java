package reivajh06.archchess.infrastructure.adapters.web.dtos;

import java.util.List;
import java.util.Map;

public record Move(int moveId, List<Map<String, String>> tiles) {}
