package a203.findit.controller;

import a203.findit.exception.CustomException;
import a203.findit.model.dto.req.User.CreateRoomDTO;
import a203.findit.model.dto.req.User.EntercodeDTO;
import a203.findit.model.dto.req.User.PlayerInfoDTO;
import a203.findit.model.dto.res.*;
import a203.findit.model.dto.req.User.RoomDTO;
import a203.findit.model.entity.*;
import a203.findit.model.repository.GameRepository;
import a203.findit.model.repository.IGTRepository;
import a203.findit.model.repository.MemoryPlayerRepository;
import a203.findit.model.repository.UserRepository;
import a203.findit.service.*;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.asm.Advice;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@CrossOrigin
public class RoomController {
    /*
    user 가 방 create, entercode, 방 시작
     */
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final RoomServiceImpl roomService;
    private final UserServiceImpl userService;
    private final PlayerServiceImpl playerService;
    private final RankingServiceImpl rankingService;
    private final GameServiceImpl gameService;
    private final IGTRepository igtRepos;
    private final GameRepository gameRepos;
    private final UserRepository userRepos;

    @PostMapping("/room/create2")
    @ResponseBody
    public ApiResponse create2(){
        UserDetails user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = user.getUsername();
        System.out.println("*****************: "+username);
        ApiResponse result = new ApiResponse();
        return result;
    }

    @GetMapping("/room/{entercode}/treasures")
    public ResponseEntity getIgt(@PathVariable("entercode") String entercode){
        List<ResIGT> result = new ArrayList<>();

        StringBuilder sb = new StringBuilder();
        String gameId = sb.append(entercode.charAt(1)).append(entercode.charAt(3)).append(entercode.charAt(5)).toString();

        List<Treasure> tidList = igtRepos.findAllByGameId(Long.parseLong(gameId))
                .stream().map(x->x.getTreasure()).collect(Collectors.toList());


        for (Treasure t : tidList) {
            ResIGT resIGT = new ResIGT();
            resIGT.setTid(t.getId());
            resIGT.setImg(t.getImageUrl());
            result.add(resIGT);
        }

        return ResponseEntity.ok().body(result);
    }

    @PostMapping("/room/create")
    public ResponseEntity create(@Valid @RequestBody CreateRoomDTO createRoomDTO){
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = principal.getUsername();

        Map<String, String> result = new HashMap<>();

        RoomDTO roomDTO = roomService.join(username, createRoomDTO.getMode(), createRoomDTO.getLimitminute());
        result.put("entercode",roomDTO.getEnterCode());
        return ResponseEntity.ok().body(result);
    }

    @MessageMapping("/open")
    public void socketOpen(String entercode) {
        JSONObject jsonObject = new JSONObject();
        RoomDTO roomDTO = roomService.find(entercode);
        if(roomDTO == null) {
            jsonObject.put("code", "no such entercode");
        }
        else if(roomDTO.getEndTime()!= null){
            jsonObject.put("code", "expired room");
        }else{
            jsonObject.put("code", "success");
            jsonObject.put("status","ready");
            jsonObject.put("mode",roomDTO.getMode());
            Optional<User> user = userService.findByUserId(roomDTO.getUserId());
            jsonObject.put("username",user.get().getUsername());
            jsonObject.put("limitminute",roomDTO.getLimitminute());
            //userid
            jsonObject.put("room",roomDTO.getRoomId());
        }
        simpMessagingTemplate.convertAndSend("/sub/room/"+entercode,jsonObject);
    }

    @MessageMapping("/gamestart")
    public void gameStart(String entercode){
        JSONObject jsonObject = new JSONObject();
        RoomDTO roomDTO = roomService.start(entercode);
//        RoomDTO roomDTO = roomService.find(entercodeDTO.getEntercode());
//        roomDTO.setStartTime(LocalDateTime.now());
        jsonObject.put("code", "success");
        jsonObject.put("status","start");
        jsonObject.put("mode",roomDTO.getMode());
        jsonObject.put("limitminute",roomDTO.getLimitminute());
        jsonObject.put("starttime",roomDTO.getStartTime());
        jsonObject.put("room",roomDTO.getRoomId());
        simpMessagingTemplate.convertAndSend("/sub/room/"+entercode,jsonObject);
    }

    @MessageMapping("finish")
    public void gameFinish(String entercode){
        roomService.finish(entercode);

        ArrayList<PlayerInfoDTO> players = playerService.rankChange(entercode);
        JSONArray rankJson = new JSONArray();
        JSONObject bef = new JSONObject();
        bef.put("status", "end");
        rankJson.add(bef);
        for( PlayerInfoDTO playerInfoDTO : players ){
            JSONObject temp = new JSONObject();
            temp.put("rank", playerInfoDTO.getRank());
            temp.put("profileImg", playerInfoDTO.getProfileImg());
            temp.put("nickname", playerInfoDTO.getNickname());
            temp.put("score", playerInfoDTO.getScore());
            temp.put("sessionId", playerInfoDTO.getSessionId());
            rankJson.add(temp);
        }
        simpMessagingTemplate.convertAndSend("/sub/rank/"+entercode,rankJson);
    }

//    @GetMapping("/room/result")
//    public ResponseEntity<List<Ranking>> sendResult(@Valid @RequestBody EntercodeDTO entercodeDTO){
//        return ResponseEntity.ok().body(rankingService.getRanks(entercodeDTO.getEntercode()));
//    }

    @GetMapping("/room/result/rank/{entercode}")
    public ResponseEntity<ArrayList<Ranking>> showResult(@Valid @PathVariable String entercode){
        return ResponseEntity.ok().body(rankingService.getRanks(entercode));
    }

    //FE 구현 안함
    @GetMapping("/room/result/info")
    public ResponseEntity<ArrayList<ResGameDTO>> showGameInfo(){
        UserDetails currUser = (UserDetails) (SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        ArrayList<Game> games = gameService.findByUsername(currUser.getUsername());

        ArrayList<ResGameDTO> result = new ArrayList<>();

        for (Game game : games) {
            ResGameDTO dto = ResGameDTO.builder()
                    .createTime(game.getCreateTime())
                    .startTime(game.getStartTime())
                    .limitMin(game.getLimitMin())
                    .endTime(game.getEndTime())
                    .mode(game.getMode())
                    .entercode(game.getEntercode())
                    .playTime(game.getPlayTime())
                    .build();
            result.add(dto);
        }

        return ResponseEntity.ok().body(result);
    }
}