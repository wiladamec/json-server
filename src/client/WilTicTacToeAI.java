package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WilTicTacToeAI {

	public static final String host = "http://localhost:8000";
	public static int id, gId, auth;
	public static Random rand = new Random();
	public static boolean gameRunning = true;
	public static int map[][] = new int[3][3];

	/**
	 * @param args
	 */
	public static void main(String[] args) throws JSONException {

		JSONObject nullObject = new JSONObject("{}");

		JSONObject playerInfo = doRequest(nullObject, "/connect");
		if (playerInfo == null) {
			System.out.println("/connect request failed");
			return;
		}
		id = playerInfo.getInt("ID");
		gId = playerInfo.getInt("GAMEID");
		auth = playerInfo.getInt("AUTH");

		JSONObject getStatusObj = new JSONObject();
		getStatusObj.put("ID", id);
		getStatusObj.put("GAMEID", gId);
		getStatusObj.put("AUTH", auth);

		while (gameRunning) {

			JSONObject gStatus = doRequest(getStatusObj, "/game/status");
			gameRunning = gStatus.getBoolean("RUNNING");

			int moves = 0;
			JSONArray board = gStatus.getJSONArray("BOARD");
			for (int i = 0; i < board.length(); i++) {
				for (int j = 0; j < board.getJSONArray(i).length(); j++) {
					map[i][j] = board.getJSONArray(i).getInt(j);
					if (map[i][j] == -1)
						moves++;
				}
			}

			if (moves == 0) {
				System.out.println("No avaliable moves");
				gameRunning = false;
				break;
			}
			
			if(gStatus.getInt("WHOWON")!=-1 && gStatus.getInt("WHOWON")!=id){
				System.out.println("We lost :(");
				break;
			}

			if (gStatus.getInt("TURN") == id && gameRunning) {

				boolean haventFoundMove = true;
				int q = -1, p = -1;
				int[] coords = new int[2];
				
				while (haventFoundMove) {
					
					coords = traverseRows();
					if (coords[0] == -1) {
						coords = traverseCols();
						if (coords[0] == -1) {
							coords = traverseDiags();
							if (coords[0] == -1) {
								
								// What goes here?
								if(map[0][0] == -1) {
									q = 0;
									p = 0;
									haventFoundMove = false;
								}
								else if(map[0][2] == -1) {
									q = 0;
									p = 2;
									haventFoundMove = false;
								}
								else if(map[2][0] == -1) {
									q = 2;
									p = 0;
									haventFoundMove = false;
								}
								else if(map[2][2] == -1) {
									q = 2;
									p = 2;
									haventFoundMove = false;
								}
								else if(map[1][1] == -1) {
									q = 1;
									p = 1;
									haventFoundMove = false;
								}
								else {
									q = rand.nextInt(3);
									p = rand.nextInt(3);
									if (map[q][p] == -1) {
										haventFoundMove = false;
									}
								}
							}
							else {
								for(int i = 0; i < 2; i++) {
									q = coords[0];
									p = coords[1];
									haventFoundMove = false;
								}
							}
						}
						else {
							for(int i = 0; i < 2; i++) {
								q = coords[0];
								p = coords[1];
								haventFoundMove = false;
							}
						}
					}
					else {
						for(int i = 0; i < 2; i++) {
							q = coords[0];
							p = coords[1];
							haventFoundMove = false;
						}
					}
					
					/*else {
						q = rand.nextInt(3);
						p = rand.nextInt(3);
						if (map[q][p] == -1) {
							haventFoundMove = false;
						}
					}*/
				}
				JSONObject moveCmd = new JSONObject();
				JSONArray mCmd = new JSONArray();
				moveCmd.put("ID", id);
				moveCmd.put("GAMEID", gId);
				moveCmd.put("AUTH", auth);
				mCmd.put(0, "MOVE");
				mCmd.put(1, q);
				mCmd.put(2, p);
				moveCmd.put("COMMAND", mCmd);
				JSONObject moveRet = doRequest(moveCmd, "/game/move");
				System.out.println(moveRet.getBoolean("WON"));
				if (moveRet.getBoolean("WON") == true) {
					System.out.println("I WON!");
					gameRunning = false;
					break;
				}

			} else {

				// if it isn't our turn give the other guy 1 second to think
				// about it before checking again
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		}

	}

	public static int[] traverseRows() {
		int enemyCount = 0;
		int[] coords = {-1, -1};
		
		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 3; j++) {
				if (map[i][j] == 1 - id) {
					enemyCount++;
				}
			}
			if(enemyCount == 2) {
				for(int j = 0; j < 3; j++) {
					if(map[i][j] == -1) {
						coords[0] = i;
						coords[1] = j;
					}
				}
			}
		}
		
		return coords;
	}
	
	public static int[] traverseCols() {
		int enemyCount = 0;
		int[] coords = {-1, -1};
		
		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 3; j++) {
				if (map[j][i] == 1 - id) {
					enemyCount++;
				}
			}
			if(enemyCount == 2) {
				for(int j = 0; j < 3; j++) {
					if(map[j][i] == -1) {
						coords[0] = j;
						coords[1] = i;
					}
				}
			}
		}
		
		return coords;
	}
	
	public static int[] traverseDiags() {
		int enemyCount = 0;
		int[] coords = {-1, -1};
		boolean moveFound = false;
		
		for (int i = 0, j = 0; i < 3 && j < 3; i++, j++) {
			if (map[i][j] == 1 - id) {
				enemyCount++;
			}
		}
		if(enemyCount == 2) {
			for (int i = 0, j = 0; i < 3 && j < 3; i++, j++) {
				if(map[i][j] == -1) {
					coords[0] = i;
					coords[1] = j;
					moveFound = true;
				}
			}
		}
		
		if(!moveFound) {
			for (int i = 2, j = 0; i >= 0 && j < 3; i--, j++) {
				if (map[i][j] == 1 - id) {
					enemyCount++;
				}
			}
			if(enemyCount == 2) {
				for (int i = 2, j = 0; i >= 0 && j < 3; i--, j++) {
					if(map[i][j] == -1) {
						coords[0] = i;
						coords[1] = j;
						moveFound = true;
					}
				}
			}
		}
		
		return coords;
	}
	
	public static JSONObject doRequest(JSONObject req, String urlPath) {
		String response = "";
		JSONObject resp = null;
		String data;
		try {
			data = URLEncoder.encode(req.toString(), "UTF-8");
			// Send data
			URL url = new URL(host + urlPath);
			URLConnection conn = url.openConnection();
			conn.setDoOutput(true);
			OutputStreamWriter wr = new OutputStreamWriter(
					conn.getOutputStream());
			wr.write(data);
			wr.flush();

			// Get the response
			BufferedReader rd = new BufferedReader(new InputStreamReader(
					conn.getInputStream()));
			String line;
			while ((line = rd.readLine()) != null) {
				response += line;
				// System.out.println(line);
			}
			wr.close();
			rd.close();
			resp = new JSONObject(response);

		} catch (UnsupportedEncodingException e) { // PRO error handling!

		} catch (MalformedURLException e) {

		} catch (IOException e) {

		} catch (JSONException e) {

		}

		return resp;
	}

}
