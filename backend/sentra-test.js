import http from "k6/http";

export const options = {
  vus: 20,
  duration: "5s",
};

export default function () {
  http.get("http://localhost:8080/api/v1/secure-data");
}