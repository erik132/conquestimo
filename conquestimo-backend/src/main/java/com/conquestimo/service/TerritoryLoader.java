package com.conquestimo.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;

@Component
public class TerritoryLoader {

    public record TerritoryInfo(String id, String name, List<String> adjacencies) {}

    private Map<String, TerritoryInfo> territoryMap;
    private List<TerritoryInfo> territories;

    @PostConstruct
    public void load() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ClassPathResource resource = new ClassPathResource("territories.json");
        List<Map<String, Object>> raw = mapper.readValue(
                resource.getInputStream(),
                new TypeReference<>() {}
        );
        territories = new ArrayList<>();
        territoryMap = new HashMap<>();
        for (Map<String, Object> entry : raw) {
            @SuppressWarnings("unchecked")
            TerritoryInfo info = new TerritoryInfo(
                    (String) entry.get("id"),
                    (String) entry.get("name"),
                    (List<String>) entry.get("adjacencies")
            );
            territories.add(info);
            territoryMap.put(info.id(), info);
        }
    }

    public List<TerritoryInfo> getAll() { return territories; }

    public TerritoryInfo get(String id) { return territoryMap.get(id); }

    public boolean areAdjacent(String a, String b) {
        TerritoryInfo info = territoryMap.get(a);
        return info != null && info.adjacencies().contains(b);
    }

    /** BFS shortest path between two territories; returns list of territory IDs including start and end */
    public List<String> shortestPath(String from, String to) {
        if (from.equals(to)) return List.of(from);
        Map<String, String> parent = new HashMap<>();
        Queue<String> queue = new LinkedList<>();
        queue.add(from);
        parent.put(from, null);
        while (!queue.isEmpty()) {
            String current = queue.poll();
            TerritoryInfo info = territoryMap.get(current);
            if (info == null) continue;
            for (String neighbor : info.adjacencies()) {
                if (!parent.containsKey(neighbor)) {
                    parent.put(neighbor, current);
                    if (neighbor.equals(to)) {
                        return buildPath(parent, from, to);
                    }
                    queue.add(neighbor);
                }
            }
        }
        return Collections.emptyList();
    }

    private List<String> buildPath(Map<String, String> parent, String from, String to) {
        LinkedList<String> path = new LinkedList<>();
        String current = to;
        while (current != null) {
            path.addFirst(current);
            current = parent.get(current);
        }
        return path;
    }
}
