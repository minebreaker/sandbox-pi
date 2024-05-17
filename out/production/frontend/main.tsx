import React from "react"
import { createRoot } from "react-dom/client"
import { Chart } from "./component/chart"


document.addEventListener("DOMContentLoaded", () => {
  const root = createRoot(document.getElementById("app")!!)
  root.render(
    <Chart />
  )
})
