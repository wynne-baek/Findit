import React, { useState } from "react";
import { useNavigate } from "react-router-dom";

import { Box, styled } from "@mui/system";
import Modal from "components/atom/Modal";
import Input from "components/atom/Input";
import CustomButton from "components/atom/CustomButton";
import CustomText from "components/atom/CustomText";
import compass from "static/compass_100.png";
import { requestLogin, requestUserInfo } from "api/user";

import ls from "helper/LocalStorage";

import { useDispatch } from "react-redux";
import { setUserInfoToStore } from "reducers/user";

import "./compass.scss";

const LoginStyle = {
  mt: "5vh",
  mb: "3vh",
  display: "flex",
  flexDirection: "column",
  alignItems: "end",
};

const ButtonBox = styled(Box)(
  () => `
  text-align: center;
  position: absolute;
  top: 60vh;
  left: 50%;
  transform: translate(-50%);
  `,
);

export default function Login() {
  const navigate = useNavigate();
  const dispatch = useDispatch();

  function goToSignup() {
    navigate("/Signup");
  }

  const [id, setId] = useState("");
  const [pw, setPw] = useState("");
  // const [userInfo, setUserInfo] = useState("");

  async function loginSuccess(res) {
    const accessToken = res.data.accessToken;
    const refreshToken = res.data.refreshToken;
    ls.set("accessToken", accessToken);
    ls.set("refreshToken", refreshToken);
    await requestUserInfo(id, getUserInfoSuccess, getUserInfoFail);
    // dispatch(setUserInfoToStore(id));
    // navigate("/hostmain");
  }

  function getUserInfoSuccess(res) {
    dispatch(setUserInfoToStore(res.data));
    navigate("/hostmain");
  }

  function getUserInfoFail(err) {
    // console.log(err);
  }

  function loginFail(res) {
    // console.log(res);
  }

  function onClickLogin(e) {
    e.preventDefault();
    requestLogin(id, pw, loginSuccess, loginFail);
  }

  function onChangeId(e) {
    const id = e.target.value;
    setId(id);
  }

  function onChangePw(e) {
    const pw = e.target.value;
    setPw(pw);
  }

  function goToMain(e) {
    navigate("/main");
  }

  return (
    <Box sx={{ textAlign: "center" }}>
      <Box sx={{ mt: "3vh" }}>
        <img src={compass} alt="compass" width="100" className="floating-small" />
      </Box>
      <Modal>
        <Box sx={{ mt: "5vh", mb: "0" }}>
          <CustomText size="xl" weight="bold">
            {"Login | "}
          </CustomText>
          <span onClick={goToSignup}>
            <CustomText size="xl" weight="bold" variant="grey">
              Signup
            </CustomText>
          </span>
        </Box>
        <Box>
          <CustomText size="xxs" variant="grey">
            ????????? ??????????????? ???????????? ????????????
          </CustomText>
        </Box>
        <Box sx={LoginStyle}>
          <Input placeholder="?????????" value={id} onChange={onChangeId} />
          <Input placeholder="????????????" value={pw} type="password" onChange={onChangePw} />
          <Box sx={{ padding: "0 5vh" }} onClick={goToMain}>
            {/* <CustomText size="xs" variant="grey">
              ??????????????? ??????????????????????
            </CustomText> */}
            <CustomText size="xs" variant="grey">
              ?????? ???????????? ????????????
            </CustomText>
          </Box>
        </Box>
        <ButtonBox>
          <CustomButton size="large" onClick={onClickLogin}>
            ?????????
          </CustomButton>
        </ButtonBox>
      </Modal>
    </Box>
  );
}
