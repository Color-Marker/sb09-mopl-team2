package com.sb09.sb09moplteam2.content.batch;

import com.sb09.sb09moplteam2.content.entity.Content;
import java.util.List;

public record ContentAndTags(Content content, List<String> tags) {
}