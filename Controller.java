package com.internshala.connectfour;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;
import javafx.animation.TranslateTransition;

import java.awt.*;
import java.net.URL;
import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Controller implements Initializable {

	private static final int COLUMNS= 7;
	private static final int ROWS= 6;
	private static final int CIRCLE_DIAMETER=80;
	private static final String discColor1= "#24303E";
	private static final String discColor2= "#4CAA88";

	private String PLAYER_ONE= "Player One";
	private String PLAYER_TWO= "Player Two";

	private boolean isPlayerOneTurn= true;

	 private Disc[][] insertedDiscsArray= new Disc[ROWS][COLUMNS];

	@FXML
	public GridPane rootGridPane;

	@FXML
	public Pane insertedDiscPane;

	@FXML
	public Label playerNameLabel;

	@FXML
	public TextField playerOneTextField;

	@FXML
	public TextField playerTwoTextField;

	@FXML
	public Button setButton;

	boolean isAllowedToInsert=true;
	
	public void createPlayground(){
		Shape rectWithHoles= new Rectangle((COLUMNS + 1) * CIRCLE_DIAMETER, (ROWS + 1) * CIRCLE_DIAMETER);

		for (int row=0; row<ROWS; row++){
			for (int col=0; col<COLUMNS; col++){
				Circle circle=new Circle();
				circle.setRadius(CIRCLE_DIAMETER/2);
				circle.setCenterX(CIRCLE_DIAMETER/2);
				circle.setCenterY(CIRCLE_DIAMETER/2);
				circle.setSmooth(true);

				circle.setTranslateX(col * (CIRCLE_DIAMETER +5) + CIRCLE_DIAMETER/4);
				circle.setTranslateY(row * (CIRCLE_DIAMETER +5) + CIRCLE_DIAMETER/4);

				rectWithHoles= javafx.scene.shape.Shape.subtract(rectWithHoles, circle);

				setButton.setOnAction(event -> {
					 PLAYER_ONE=playerOneTextField.getText();
					 PLAYER_TWO=playerTwoTextField.getText();
				});


			}
		}

		rectWithHoles.setFill(Color.WHITE);
		rootGridPane.add((Node) rectWithHoles, 0, 1);

		List<Rectangle> rectangleList= createClickableColumns();

		for (Rectangle rectangle: rectangleList){
			rootGridPane.add(rectangle, 0, 1);
		}
	}

	 public List<Rectangle> createClickableColumns(){

		List<Rectangle> rectangleList=new ArrayList<>();

		for(int col=0; col<COLUMNS; col++) {

			Rectangle rectangle = new Rectangle(CIRCLE_DIAMETER, (ROWS + 1) * CIRCLE_DIAMETER);
			rectangle.setFill(Color.TRANSPARENT);
			rectangle.setTranslateX(col * (CIRCLE_DIAMETER + 5) + CIRCLE_DIAMETER / 4);

			rectangle.setOnMouseDragEntered(event -> rectangle.setFill(Color.valueOf("#eeeeeee26")));
			rectangle.setOnMouseDragExited(event -> rectangle.setFill(Color.TRANSPARENT));

			final int column= col;
			rectangle.setOnMouseClicked(event -> {
				if (isAllowedToInsert) {
					isAllowedToInsert=false;    //Allow one disc at a time
					insertDisc(new Disc(isPlayerOneTurn), column);
				}
			});

			rectangleList.add(rectangle);
		}

		return rectangleList;
	}

	private void insertDisc(Disc disc, int column){

		int row= ROWS - 1;

		while (row>=0){
			if(getDiscIfPresent(row,column) == null)
				break;
			row--;
		}

		//It simply do nothing.
		if (row< 0)
			return;

		//For structural Changes:For Developers
		insertedDiscsArray[row][column]=disc;

		//For Visual Changes:For User
		insertedDiscPane.getChildren().add(disc);

		disc.setTranslateX(column * (CIRCLE_DIAMETER + 5) + CIRCLE_DIAMETER/4);

		int currentRow= row;
		TranslateTransition translateTransition=new TranslateTransition(Duration.seconds(0.5),disc);
		translateTransition.setToY(row * (CIRCLE_DIAMETER + 5) + CIRCLE_DIAMETER/4);

		translateTransition.setOnFinished(event -> {
			isAllowedToInsert=true;
			if (gameEnded(currentRow, column)){
				gameOver();
				return;
			}

			isPlayerOneTurn= !isPlayerOneTurn;
			playerNameLabel.setText(isPlayerOneTurn? PLAYER_ONE : PLAYER_TWO);
		});

		translateTransition.play();
	}

	private boolean gameEnded(int row, int column){

		//Vertical Points
		List<Point2D> verticalPoints= IntStream.rangeClosed(row-3, row+3)
				.mapToObj(r ->new Point2D(r, column))
				.collect(Collectors.toList());

		//Horizontal Points
		List<Point2D> horizontalPoints= IntStream.rangeClosed(column-3, column+3)
				.mapToObj(c ->new Point2D(row, c))
				.collect(Collectors.toList());

		//Diagonal 1
		Point2D startPoint1= new Point2D(row-3,column+3);
		List<Point2D> diaginal1Points= IntStream.rangeClosed(0, 6)
				.mapToObj(i -> startPoint1.add(i, -i))
				.collect(Collectors.toList());

		//Diagonal 2
		Point2D startPoint2= new Point2D(row-3,column-3);
		List<Point2D> diaginal2Points= IntStream.rangeClosed(0, 6)
				.mapToObj(i -> startPoint2.add(i, i))
				.collect(Collectors.toList());

		boolean isEnded= checkCombinations(verticalPoints) || checkCombinations(horizontalPoints)
			|| checkCombinations(diaginal1Points) ||checkCombinations(diaginal2Points);
		return isEnded;
	}


	private boolean checkCombinations(List<Point2D> points) {
		int chain=0;
		for (Point2D point : points) {
				int rowIndexForArray= (int) point.getX();
				int columnIndexForArray= (int) point.getY();

				Disc disc= getDiscIfPresent(rowIndexForArray, columnIndexForArray);

				if (disc != null && disc.isPlayerOneMove == isPlayerOneTurn){

					chain++;
					if (chain==4){
						return true;
					}
				}
				else {
					chain=0;
				}
		}
		return false;
	}

	//To prevent Array Out Of Bound Exception
	private Disc getDiscIfPresent(int row, int column){
		
		if (row>=ROWS || row<0 || column>=COLUMNS || column<0) {
			return null;
		}
			return insertedDiscsArray[row][column];

	}
	
	private void gameOver(){
		String winner= isPlayerOneTurn ? PLAYER_ONE : PLAYER_TWO;
		System.out.println("Winner is:"+ winner);

		Alert alert= new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("Connect Four");
		alert.setHeaderText("The Winner is "+winner);
		alert.setContentText("Want to play again?");

		ButtonType yesButton= new ButtonType("Yes");
		ButtonType noButton= new ButtonType("No");
		alert.getButtonTypes().setAll(yesButton, noButton);

		Platform.runLater(()->{
			Optional<ButtonType> buttonClicked= alert.showAndWait();
			if (buttonClicked.isPresent() && buttonClicked.get() == yesButton){
				resetGame();
			}
			else {
				Platform.exit();
				System.exit(0);
			}
		});
	}

	public void resetGame() {      //Remove all Discs

		insertedDiscPane.getChildren().clear();
		for (int row=0; row<insertedDiscsArray.length; row++) {      //Clear i.e; Back to null
			for (int col = 0; col < insertedDiscsArray[row].length; col++) {
				insertedDiscsArray[row][col] = null;
			}
		}
		isPlayerOneTurn=true; //Let player One start the game
		playerNameLabel.setText(PLAYER_ONE);

		createPlayground(); //Prepare Fresh Game
	}
//=============================================================================
	private static class Disc extends Circle{

		private final boolean isPlayerOneMove;

		public Disc(boolean isPlayerOneMove){

			this.isPlayerOneMove= isPlayerOneMove;
			setRadius(CIRCLE_DIAMETER/2);
			setFill(isPlayerOneMove? Color.valueOf(discColor1): Color.valueOf(discColor2));
			setCenterX(CIRCLE_DIAMETER/2);
			setCenterY(CIRCLE_DIAMETER/2);
		}
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

	}
}
