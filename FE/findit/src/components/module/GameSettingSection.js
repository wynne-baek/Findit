import React, { useEffect, useState } from "react";

import { Box, styled } from "@mui/system";

import CustomText from "components/atom/CustomText";
import GameSettingBox from "components/atom/GameSettingBox";
import CircleButton from "components/atom/CircleButton";

import TimerIcon from "static/timer_clock.svg";
import TreasureIcon from "static/wrapped_gift.svg";

import { requestGameConfiguration } from "api/host";

import { useNavigate } from "react-router-dom";
import ls from "helper/LocalStorage";

const StyledTextBox = styled(Box)(
  () => `
display: flex;
flex-direction: column;
position: relative;
`,
);

const TimerSettingBox = styled(Box)(
  () => `
display: flex;
justify-content: center;
align-items: center;
margin: 20px 0;
`,
);

const TimerNum = styled(Box)(
  () => `
background: white;
width: 60px;
height: 60px;
border-radius: 10px;
display: flex;
justify-content: center;
align-items: center;
margin: 0 10px;
`,
);

const HandlerButtom = styled("button")(
  () => `
width: 30px;
height: 30px;
background: #DA989A;
border: 0;
border-radius: 5px;
color: white;
font-size: 20px;
font-weight: bold;
margin: 0 20px;
padding: 0;
`,
);

const StyledIcon = styled("img")(
  () => `
position: absolute;
height: 9vh;
left: 200px;
top: -50px;
`,
);

const ModeButtonBox = styled(Box)(
  () => `
margin: 30px 30px 5px;
display: flex;
justify-content: space-around;
`,
);

const SelectedModeButton = styled("button")(
  () => `
border: 0;
width: 98px;
height: 42px;
background: #9FAFD8;
border-radius: 15px;
box-shadow: inset 1px 1px 2px #babecc , inset -1px -1px 2px #ffffff;
cursor: pointer;
`,
);

const ModeButton = styled("button")(
  () => `
border: 0;
width: 98px;
height: 42px;
background: white;
border-radius: 15px;
box-shadow: -2px -2px 5px #ffffff, 2px 2px 5px #babecc;
transition: all 0.2s ease-in-out;
cursor: pointer;
`,
);

const ModeInfoText = styled(Box)(
  () => `
text-align: center;
margin-top: 8px;
`,
);

const NextButtonBox = styled(Box)(
  () => `
margin: 3vh 10vw;
text-align: end
`,
);

export default function GameSettingSection() {
  const navigate = useNavigate();
  const [timer, setTimer] = useState(10);
  const [entercode, setEnterCode] = useState(0);

  function incrementHandler() {
    if (timer >= 60) {
      setTimer(60);
    } else {
      setTimer(timer + 5);
    }
  }

  function decrementHandler() {
    if (timer <= 0) {
      setTimer(0);
    } else {
      setTimer(timer - 5);
    }
  }

  const [modeName, setModeName] = useState("GENERAL");

  function invertModeHandler() {
    if (modeName === "GENERAL") {
      setModeName("RANDOM");
    } else if (modeName === "RANDOM") {
      setModeName("GENERAL");
    }
  }

  function SelectNormalMode() {
    return (
      <Box>
        <ModeButtonBox>
          <SelectedModeButton>
            <CustomText size="s" weight="normal" variant="white">
              ?????? ??????
            </CustomText>
          </SelectedModeButton>
          <ModeButton onClick={invertModeHandler}>
            <CustomText size="s" weight="normal" variant="primary">
              ?????? ??????
            </CustomText>
          </ModeButton>
        </ModeButtonBox>
        <ModeInfoText>
          <CustomText variant="primary" size="xxs" margin="3">
            * ????????? ?????? ???????????? ????????? ???????????? ????????? *
          </CustomText>
        </ModeInfoText>
      </Box>
    );
  }

  function SelectRandomMode() {
    return (
      <Box>
        <ModeButtonBox>
          <ModeButton onClick={invertModeHandler}>
            <CustomText size="s" weight="normal" variant="primary">
              ?????? ??????
            </CustomText>
          </ModeButton>
          <SelectedModeButton>
            <CustomText size="s" weight="normal" variant="white">
              ?????? ??????
            </CustomText>
          </SelectedModeButton>
        </ModeButtonBox>
        <ModeInfoText>
          <CustomText variant="primary" size="xxs" margin="3">
            * ????????? ?????? ??? ???????????? ????????? ???????????? ??????????????? *
          </CustomText>
        </ModeInfoText>
      </Box>
    );
  }

  function gameConfigurationSuccess(res) {
    setEnterCode(res.data.entercode);
  }

  useEffect(() => {
    if (entercode !== 0) {
      ls.set('entercode', entercode)
      navigate(`/treasure/${entercode}`)
    }
  }, [entercode, navigate])

  function gameConfigurationFail(res) {
    // alert ?????????
    // console.log("?????? ?????? ??????", res);
  }

  function postGameConfiguration(event) {
    event.preventDefault();
    // console.log(timer, modeName);
    requestGameConfiguration(timer, modeName, gameConfigurationSuccess, gameConfigurationFail);
  }

  return (
    <Box>
      <GameSettingBox>
        <StyledTextBox>
          <StyledIcon src={TimerIcon} alt="timer icon" />
          <CustomText variant="secondary" size="l" weight="bold">
            ?????? ??????
          </CustomText>
          <CustomText variant="secondary" size="xxs" margin="3">
            5??? ????????? ?????? ???????????????.
          </CustomText>
          <TimerSettingBox>
            <HandlerButtom onClick={decrementHandler}>-</HandlerButtom>
            <TimerNum>
              <CustomText variant="secondary" weight="bold" size="xl">
                {timer}
              </CustomText>
            </TimerNum>
            <CustomText variant="secondary" weight="bold" size="xl">
              min
            </CustomText>
            <HandlerButtom onClick={incrementHandler}>+</HandlerButtom>
          </TimerSettingBox>
        </StyledTextBox>
      </GameSettingBox>
      <Box sx={{ height: "10px" }}></Box>
      <GameSettingBox variant="primaryWeek">
        <StyledTextBox>
          <StyledIcon src={TreasureIcon} alt="timer icon" />
          <CustomText variant="primary" size="l" weight="bold">
            ?????? ??????
          </CustomText>
        </StyledTextBox>
        {modeName === "RANDOM" ? (
          <SelectRandomMode></SelectRandomMode>
        ) : (
          <SelectNormalMode></SelectNormalMode>
        )}
      </GameSettingBox>
      <NextButtonBox onClick={postGameConfiguration}>
        <CircleButton icon="next" size="smallest" />
      </NextButtonBox>
    </Box>
  );
}
