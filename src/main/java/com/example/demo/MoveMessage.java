package com.example.demo;

import com.myProject.Move;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MoveMessage {
    private UUID gameUuid;
    private Move move;
}
