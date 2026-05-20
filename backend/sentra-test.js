import http from "k6/http";

export const options = {
  vus: 450,
  duration: "40s",
};

export default function () {
  http.get("http://localhost:8080/api/v1/secure-data");
}