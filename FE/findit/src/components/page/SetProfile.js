import React, { useState } from "react";
import { useNavigate, useParams } from "react-router-dom";


import { Box } from "@mui/system";

import CircleButton from "components/atom/CircleButton";
import CustomButton from "components/atom/CustomButton";
import Input from "components/atom/Input";
import CustomText from "../atom/CustomText";
import ProfileImage from "../atom/ProfileImage";
import RefreshIcon from "static/refresh.png";

import { requestLogout } from "api/user";
import { requestUpdateProfile } from "api/host";

const ProfileBoxStyle = {
  margin: "auto",
  textAlign: "center",
};

const BoxStyle = {
  textAlign: "center",
  margin: "6vh auto",
};

const IconStyle = {
  position: "absolute",
  left: "65%",
};

function PlayerProfile() {
  const [nickname, setNickname] = useState();
  const navigate = useNavigate();
  const { gameid } = useParams();
  const [imgNum, setImgNum] = useState("1");

  function onClickRefresh() {
    setImgNum(Math.floor(Math.random() * 10));
  }

  function onChangeNickname(e) {
    const nickname = e.target.value;
    setNickname(nickname);
  }

  function sendPlayerToWaiting(e) {
    e.preventDefault();
    navigate(`/waiting/${gameid}`, { state: { imgNum: imgNum, nickname: nickname } })
  }

  return (
    <Box sx={ProfileBoxStyle}>
      <Box sx={BoxStyle}>
        <CustomText size="xxl" weight="bold">
          프로필 설정
        </CustomText>
      </Box>
      <Box sx={BoxStyle}>
        <ProfileImage type="rounded" num={imgNum}></ProfileImage>
        <Box sx={IconStyle} onClick={onClickRefresh}>
          <img src={RefreshIcon} alt="refresh" width="25px" />
        </Box>
      </Box>
      <Box>
        <CustomText>닉네임을 등록해주세요</CustomText>
        <Input type="text" placeholder="닉네임" onChange={onChangeNickname}></Input>
      </Box>
      <CustomButton size="large" color="primary" onClick={sendPlayerToWaiting}>
        확인
      </CustomButton>
    </Box>
  );
}

function HostProfile() {
  const navigate = useNavigate();

  function logoutSuccess() {
    navigate("/main");
  }

  function logoutFail(err) {
    console.log(err);
  }

  function onClickLogout(event) {
    event.preventDefault();
    console.log("로그아웃 버튼 클릭");
    requestLogout(logoutSuccess, logoutFail);
  }

  const [imgNum, setImgNum] = useState("0");
  const [nickname, setNickname] = useState("");

  function onClickRefresh() {
    setImgNum(Math.floor(Math.random() * 10));
  }

  function onChangeNickname(event) {
    const nickname = event.target.value;
    setNickname(nickname);
  }

  function updateProfileSuccess() {
    navigate("/hostmain");
  }

  function updateProfileFail(err) {
    console.log(err);
  }

  function onClickUpdateProfile(event) {
    event.preventDefault();
    requestUpdateProfile(imgNum, nickname, updateProfileSuccess, updateProfileFail);
  }

  // 로그아웃 함수 작성
  return (
    <Box sx={ProfileBoxStyle}>
      <Box sx={{ textAlign: "end", marginTop: "4vh", marginRight: "10vw" }} onClick={onClickLogout}>
        <CircleButton icon="logout" size="smaller"></CircleButton>
      </Box>
      <Box sx={BoxStyle}>
        <CustomText size="xxl" weight="bold">
          마이 페이지
        </CustomText>
      </Box>
      <Box sx={BoxStyle}>
        <ProfileImage type="rounded" num={imgNum}></ProfileImage>
        <Box sx={IconStyle} onClick={onClickRefresh}>
          <img src={RefreshIcon} alt="refresh" width="25px" />
        </Box>
      </Box>

      <Box>
        <CustomText>닉네임을 등록해주세요</CustomText>
        <Input type="text" placeholder="닉네임" onChange={onChangeNickname}></Input>
      </Box>
      <CustomButton size="small" color="secondary">
        비밀번호 변경
      </CustomButton>
      <CustomButton size="small" color="primary" onClick={onClickUpdateProfile}>
        프로필 변경
      </CustomButton>
    </Box>
  );
}

export default function SetProfile({ target }) {
  function isPlayer(target) {
    if (target === "user") {
      return false;
    } else {
      return true;
    }
  }

  return (
    <Box>{isPlayer(target) ? <PlayerProfile></PlayerProfile> : <HostProfile></HostProfile>}</Box>
  );
}
