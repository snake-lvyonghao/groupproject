import axios from "axios";

const username="user";
const password="123";

const authString=`${username}:${password}`;
const base64AuthString=btoa(authString);


export default axios.create({
  //在这里填入API的根目录
  baseURL: "http://localhost:8085/api/",
  params: {},
  headers:{
    "Authorization": `Basic ${base64AuthString}`,
    "Content-Type": "application/json"  
  }
});
