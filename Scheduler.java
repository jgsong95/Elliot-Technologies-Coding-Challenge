package elliot;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class Scheduler {
	
	public static void main(String[] args) {
		
		HashMap<Integer, ArrayList<Event>> schedule = getEvents();
		
		for (int i = 1; i <= 7; i++) {
			if(schedule.get(i).isEmpty()) {
				System.out.println("Everyone is free on all of day " + i);
			} else {
				ArrayList<Event> consolidatedEvents = consolidateEvents(schedule.get(i));
				
				Event prev = consolidatedEvents.get(0);
				Event freeBlock = new Event(consolidatedEvents.get(0).getEndDate(), consolidatedEvents.get(1).getStartDate());
				Duration maxDuration = Duration.between(freeBlock.getStartDate(), freeBlock.getEndDate());

				for (Event event : consolidatedEvents) {

					Duration d = Duration.between(prev.getEndDate(), event.getStartDate());
					
					if(d.getSeconds() > maxDuration.getSeconds()) {
						freeBlock.setStartDate(prev.getEndDate());
						freeBlock.setEndDate(event.getStartDate());
						maxDuration = Duration.between(event.getStartDate(), event.getEndDate());
					}
					prev = event;
				}

				System.out.println("Longest free period on day " + i + " is from " + freeBlock.getStartDate() + " to " + freeBlock.getEndDate());
			}
		}	
	}
	
	private static ArrayList<Event> consolidateEvents(ArrayList<Event> events) {
		
		if(events.size() == 0) {
			return events;
		}
		
		Comparator<Event> comparator = new Comparator<Event>()
		{

		    public int compare(Event eventA,
		            Event eventB)
		    {
		        return eventA.getStartDate().compareTo(eventB.getStartDate());
		    }

		};
		
		Collections.sort(events, comparator);
		
		LocalDateTime first = events.get(0).getStartDate();
		
		LocalDateTime timeZero = LocalDateTime.of(first.getYear(), first.getMonth(), first.getDayOfMonth(), 0, 0);
		LocalDateTime timeEightAM = LocalDateTime.of(first.getYear(), first.getMonth(), first.getDayOfMonth(), 8, 0);
		LocalDateTime timeTenPM = LocalDateTime.of(first.getYear(), first.getMonth(), first.getDayOfMonth(), 22, 0);
		LocalDateTime midnight = LocalDateTime.of(first.getYear(), first.getMonth(), first.getDayOfMonth(), 23, 59);
		
		Event morning = new Event(timeZero, timeEightAM);
		Event night = new Event(timeTenPM, midnight);
		
		
		ArrayList<Event> consolidated = new ArrayList<Event>();
		consolidated.add(morning);
		consolidated.add(events.get(0));
		
		LocalDateTime end = events.get(0).endDate;
		
		for(Event event : events) {
			if(event.startDate.isBefore(end)) {
				end = Event.maxDate(end, event.endDate);
			} else {
				consolidated.add(event);
				end = event.endDate;
			}
		}
		consolidated.add(night);
		
		return consolidated;
	}

	private static HashMap<Integer, ArrayList<Event>> getEvents() {
		String filePath = "calendar.csv";
		BufferedReader br = null;
		String line;

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime now = LocalDateTime.now();

		HashMap<Integer, ArrayList<Event>> schedule = new HashMap<Integer, ArrayList<Event>>();
		ArrayList<Event> day1 = new ArrayList<Event>();
		ArrayList<Event> day2 = new ArrayList<Event>(); 
		ArrayList<Event> day3 = new ArrayList<Event>(); 
		ArrayList<Event> day4 = new ArrayList<Event>(); 
		ArrayList<Event> day5 = new ArrayList<Event>(); 
		ArrayList<Event> day6 = new ArrayList<Event>(); 
		ArrayList<Event> day7 = new ArrayList<Event>(); 
		
		try {
		br = new BufferedReader(new FileReader(filePath));
		
			while((line = br.readLine()) != null) {
				
				String[] userEvent = line.split(", ");
				
				LocalDateTime eventStart = LocalDateTime.parse(userEvent[1], formatter);
				LocalDateTime eventEnd = LocalDateTime.parse(userEvent[2], formatter);
				
				boolean lessThanWeek = (now.isBefore(eventStart)) && now.plusWeeks(1).isAfter(eventEnd);
				boolean inTimeRange = (eventStart.getHour() > 7) && (eventEnd.getHour() < 21);
				
				if(lessThanWeek && inTimeRange) {
					Event event = new Event(eventStart, eventEnd);
					
					int day = eventStart.getDayOfYear() - now.getDayOfYear();
					switch(day) {
					case 0:
						day1.add(event);
						break;
					case 1:
						day2.add(event);
						break;
					case 2:
						day3.add(event);
						break;
					case 3:
						day4.add(event);
						break;
					case 4:
						day5.add(event);
						break;
					case 5:
						day6.add(event);
						break;
					case 6:
						day7.add(event);
						break;
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
            e.printStackTrace();
		} finally {
			if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
		}
		
		schedule.put(1, day1);
		schedule.put(2, day2);
		schedule.put(3, day3);
		schedule.put(4, day4);
		schedule.put(5, day5);
		schedule.put(6, day6);
		schedule.put(7, day7);
		
		return schedule;
	}
}

class Event {
	public LocalDateTime startDate, endDate;
	
	public Event(LocalDateTime startDate, LocalDateTime endDate) {
		this.startDate = startDate;
		this.endDate = endDate;
	}
	
	public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
	public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
	
	public LocalDateTime getStartDate() { return startDate; }
	public LocalDateTime getEndDate() { return endDate; }
	
	public static LocalDateTime maxDate(LocalDateTime a, LocalDateTime b) {
		if(a.isAfter(b)) {
			return a;
		} else {
			return b;
		}
	}
}
