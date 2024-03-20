package rip.deadcode.sandbox_pi.http.handler.ui

import cats.effect.IO
import com.google.common.net.MediaType
import com.google.inject.{Inject, Singleton}
import org.eclipse.jetty.server.Request
import rip.deadcode.sandbox_pi.http.HttpResponse.StringHttpResponse
import rip.deadcode.sandbox_pi.http.handler.ui.UiHandler.Body
import rip.deadcode.sandbox_pi.http.{HttpHandler, HttpResponse}

import scala.util.matching.compat.Regex

// temporal ui impl.
// gonna create spa as a separate project

@Singleton
class UiHandler @Inject() () extends HttpHandler {

  override def url: Regex = "^/ui$".r

  override def method: String = "GET"

  override def handle(request: Request): IO[HttpResponse] = {
    IO {

      StringHttpResponse(
        200,
        MediaType.HTML_UTF_8,
        Body
      )
    }
  }
}

private object UiHandler {
  val Body =
    // language=HTML
    """<!DOCTYPE html>
      |<html lang="en">
      |<head>
      |  <title>sandbox-pi</title>
      |  <script src="https://code.highcharts.com/highcharts.js"></script>
      |  <script src="http://code.highcharts.com/highcharts-more.js"></script>
      |</head>
      |<body style="margin: 0">
      |  <div style="width: 100dvw; height: 100dvh; display: grid; grid-template-columns: 1fr 1fr;">
      |    <div id="c_t" style="width: 50dvw; height: 50dvh;"></div>
      |    <div id="c_p" style="width: 50dvw; height: 50dvh;"></div>
      |    <div id="c_h" style="width: 50dvw; height: 50dvh;"></div>
      |    <div id="c_c" style="width: 50dvw; height: 50dvh;"></div>
      |  </div>
      |  <script>
      |    document.addEventListener("DOMContentLoaded", () => {
      |      fetch("http://192.168.0.5:8080/stat?p=day").then(response => {
      |        if (response.ok) {
      |          return response.json()
      |        } else {
      |          throw new Error(`Response: ${response.status} ${response.statusText}}`)
      |        }
      |      }).then(data => {
      |        console.log(data)
      |        const temp = Object
      |          .entries(data.temperature)
      |          .sort(([x, _1], [y, _2]) => x - y)
      |        const tempMed = temp.map(([_, v]) => v?.medianRaw ?? null)
      |        const tempMinMax = temp.map(([_, v]) => [v?.minRaw ?? null, v?.maxRaw ?? null])
      |        Highcharts.chart("c_t", {
      |          title: {
      |            text: "Temperature"
      |          },
      |          xAxis: {
      |          },
      |          yAxis: {
      |          },
      |          series: [
      |            {
      |              name: "Temperature",
      |              data: tempMed,
      |              marker: {
      |              },
      |              connectNulls: true
      |            },
      |            {
      |              name: "Max-Min",
      |              type: "arearange",
      |              data: tempMinMax,
      |              connectNulls: true
      |            }
      |          ]
      |        })
      |        const p = Object
      |          .entries(data.pressure)
      |          .sort(([x, _1], [y, _2]) => x - y)
      |        const pMed = p.map(([_, v]) => v?.medianRaw ?? null)
      |        const pMinMax = p.map(([_, v]) => [v?.minRaw ?? null, v?.maxRaw ?? null])
      |        Highcharts.chart("c_p", {
      |          title: {
      |            text: "Pressure"
      |          },
      |          xAxis: {
      |          },
      |          yAxis: {
      |          },
      |          series: [
      |            {
      |              name: "Pressure",
      |              data: pMed,
      |              marker: {
      |              },
      |              connectNulls: true
      |            },
      |            {
      |              name: "Max-Min",
      |              type: "arearange",
      |              data: pMinMax,
      |              connectNulls: true
      |            }
      |          ]
      |        })
      |        const h = Object
      |          .entries(data.humidity)
      |          .sort(([x, _1], [y, _2]) => x - y)
      |        const hMed = h.map(([_, v]) => v?.medianRaw ?? null)
      |        const hMinMax = h.map(([_, v]) => [v?.minRaw ?? null, v?.maxRaw ?? null])
      |        Highcharts.chart("c_h", {
      |          title: {
      |            text: "Humidity"
      |          },
      |          xAxis: {
      |          },
      |          yAxis: {
      |          },
      |          series: [
      |            {
      |              name: "Humidity",
      |              data: hMed,
      |              marker: {
      |              },
      |              connectNulls: true
      |            },
      |            {
      |              name: "Max-Min",
      |              type: "arearange",
      |              data: hMinMax,
      |              connectNulls: true
      |            }
      |          ]
      |        })
      |        const c = Object
      |          .entries(data.co2)
      |          .sort(([x, _1], [y, _2]) => x - y)
      |        const cMed = c.map(([_, v]) => v?.medianRaw ?? null)
      |        const cMinMax = c.map(([_, v]) => [v?.minRaw ?? null, v?.maxRaw ?? null])
      |        Highcharts.chart("c_c", {
      |          title: {
      |            text: "CO2"
      |          },
      |          xAxis: {
      |          },
      |          yAxis: {
      |          },
      |          series: [
      |            {
      |              name: "CO2",
      |              data: cMed,
      |              marker: {
      |              },
      |              connectNulls: true
      |            },
      |            {
      |              name: "Max-Min",
      |              type: "arearange",
      |              data: cMinMax,
      |              connectNulls: true
      |            }
      |          ]
      |        })
      |      })
      |    })
      |  </script>
      |</body>
      |</html>
      |""".stripMargin
}
