package com.xhstormr.app

import java.io.File
import java.net.URL
import java.text.NumberFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.ResourceBundle
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.control.Button
import javafx.scene.control.ChoiceBox
import javafx.scene.control.Dialog
import javafx.scene.control.Label
import javafx.scene.control.MenuItem
import javafx.scene.control.ProgressBar
import javafx.scene.control.ProgressIndicator
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.control.Tooltip
import javafx.scene.input.Clipboard
import javafx.scene.input.DataFormat
import javafx.stage.FileChooser
import javafx.util.Duration
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.javafx.awaitPulse
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono

class MainController : Initializable, CoroutineScope {

    @FXML
    private lateinit var tableView: TableView<ReadOnlyHttpResponse>
    @FXML
    private lateinit var tableViewCodeColumn: TableColumn<ReadOnlyHttpResponse, Int>
    @FXML
    private lateinit var tableViewLengthColumn: TableColumn<ReadOnlyHttpResponse, Long>
    @FXML
    private lateinit var tableViewPayloadColumn: TableColumn<ReadOnlyHttpResponse, String>
    @FXML
    private lateinit var tableViewCopyMenu: MenuItem
    @FXML
    private lateinit var urlField: TextField
    @FXML
    private lateinit var urlFieldTip: Tooltip
    @FXML
    private lateinit var headersField: TextArea
    @FXML
    private lateinit var payloadField: TextField
    @FXML
    private lateinit var payloadButton: Button
    @FXML
    private lateinit var methodBox: ChoiceBox<HttpMethod>
    @FXML
    private lateinit var methodBoxTip: Tooltip
    @FXML
    private lateinit var startButton: Button
    @FXML
    private lateinit var stopButton: Button
    @FXML
    private lateinit var progressBar: ProgressBar
    @FXML
    private lateinit var loadingBar: ProgressIndicator
    @FXML
    private lateinit var exitMenu: MenuItem
    @FXML
    private lateinit var aboutMenu: MenuItem
    @FXML
    private lateinit var time: Label
    @FXML
    private lateinit var status: Label
    @FXML
    private lateinit var workDone: Label
    @FXML
    private lateinit var totalWork: Label
    @FXML
    private lateinit var speed: Label

    private val webClient = WebClient.create()

    private val timeFormatter = DateTimeFormatter.ISO_LOCAL_TIME

    private val numberFormatter = NumberFormat.getInstance().apply {
        minimumFractionDigits = 3
        maximumFractionDigits = 3
    }

    private val fileChooser = FileChooser().apply {
        title = "Select File"
        initialDirectory = File(System.getProperty("user.dir"))
        extensionFilters.add(FileChooser.ExtensionFilter("Text Files", "*.txt"))
    }

    private var job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun initialize(location: URL, resources: ResourceBundle?) {
        with(methodBox) {
            items.addAll(HttpMethod.values())
            selectionModel.select(HttpMethod.HEAD)
        }

        urlFieldTip.showDelay = Duration.ZERO
        methodBoxTip.showDelay = Duration.ZERO

        tableViewCodeColumn.setCellValueFactory { it.value.statusCodeProperty }
        tableViewLengthColumn.setCellValueFactory { it.value.contentLengthProperty }
        tableViewPayloadColumn.setCellValueFactory { it.value.payloadProperty }

        tableViewCopyMenu.setOnAction {
            tableView.selectionModel.selectedItem?.let {
                Clipboard.getSystemClipboard().setContent(mapOf(DataFormat.PLAIN_TEXT to it.url))
            }
        }

        payloadButton.setOnAction {
            fileChooser.showOpenDialog(payloadButton.scene.window)?.run {
                payloadField.text = toString()
                fileChooser.initialDirectory = parentFile
            }
        }

        aboutMenu.setOnAction {
            val dialog = Dialog<String>()
            dialog.title = "About Sniper"
            dialog.dialogPane = FXMLLoader.load(ClassLoader.getSystemResource("views/about.fxml"))
            dialog.initOwner(payloadButton.scene.window)
            dialog.showAndWait()
        }

        startButton.setOnAction {
            doStart()
        }

        stopButton.setOnAction {
            doStop()
        }

        exitMenu.setOnAction {
            Platform.exit()
        }
    }

    private fun doStart() {
        val payload = File(payloadField.text)
        if (!payload.isFile) return

        val url = urlField.text
        if (url.isNullOrEmpty()) return

        tableView.items.clear()
        status.text = "Running"
        startButton.isDisable = true
        loadingBar.progress = ProgressIndicator.INDETERMINATE_PROGRESS

        val uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(url)

        val lines = payload.readLines()

        val channel = produce(Dispatchers.IO, Channel.Factory.BUFFERED) {
            for (line in lines) {
                val uri = uriComponentsBuilder.buildAndExpand(line).encode().toUri()
                val httpMethod = methodBox.selectionModel.selectedItem
                val headers: Map<String, String> = headersField.text
                    .lines()
                    .filter { it.isNotBlank() }
                    .map { it.split(':', limit = 2) }
                    .groupingBy { it[0] }
                    .aggregate { _, _, element, _ -> element[1] }
                val responseEntity = webClient.method(httpMethod)
                    .uri(uri)
                    .headers { it.setAll(headers) }
                    .retrieve()
                    .onStatus({ true }, { Mono.empty() })
                    .toBodilessEntity()
                    .onErrorReturn(ResponseEntity.badRequest().build())
                    .awaitSingle()
                val httpResponse = ReadOnlyHttpResponse(
                    line,
                    responseEntity.statusCodeValue,
                    responseEntity.headers.contentLength,
                    uri.toString()
                )
                send(httpResponse)
            }
        }

        launch {
            var count = 0
            val l = System.nanoTime()
            val size = lines.size
            totalWork.text = size.toString()
            while (!channel.isClosedForReceive) {
                awaitPulse()
                val mutableList = mutableListOf<ReadOnlyHttpResponse>()
                while (!channel.isEmpty) {
                    mutableList.add(channel.poll() ?: break)
                }
                val diff = System.nanoTime() - l
                count += mutableList.size
                time.text = timeFormatter.format(LocalTime.ofNanoOfDay(diff))
                speed.text = numberFormatter.format(1.0 * 1_000_000_000 * count / diff)
                workDone.text = count.toString()
                progressBar.progress = 1.0 * count / size
                tableView.items.addAll(mutableList)
            }
        }.also {
            it.invokeOnCompletion { doStop() }
        }
    }

    private fun doStop() {
        job.cancel()
        job = Job()
        status.text = "Stopped"
        startButton.isDisable = false
        loadingBar.progress = 1.0
    }
}

/*
ExchangeFilterFunction.ofRequestProcessor()
ExchangeFilterFunction.ofResponseProcessor()
*/
