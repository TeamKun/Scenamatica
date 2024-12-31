import json
import os
import requests
from datetime import datetime

EVENTS_MASTER = "https://raw.githubusercontent.com/sya-ri/spigot-event-list/refs/heads/master/data/events.json"  # Replace with the actual URL
EVENTS_FILE = "events.json"


def fetch_events_new():
    response = requests.get(EVENTS_MASTER)
    response.raise_for_status()  # Ensure we handle errors cleanly
    return response.json()


def fetch_events_old():
    if os.path.exists(EVENTS_FILE):
        with open(EVENTS_FILE, "r", encoding="utf-8") as f:
            return json.load(f)
    else:
        print("No existing events.json found. Creating a new one.")
        return []


def backup_old_events():
    if os.path.exists(EVENTS_FILE):
        timestamp = datetime.now().strftime("%Y%m%d")
        backup_name = f"{EVENTS_FILE}.{timestamp}.bck"
        os.rename(EVENTS_FILE, backup_name)
        print(f"Backup created: {backup_name}")


def is_compatible_event(event):
    return event["source"] in ["spigot", "bukkit", "paper"]


def update_events(events_old, events_new):
    new_event_names = {event["name"] for event in events_new["events"]}
    added_events = []
    modified_events = []
    updated_events = []
    removed_events = []

    # Updating existing events
    for old_event in events_old:
        if old_event["name"] in new_event_names:
            matching_new_event = next(
                (event for event in events_new["events"] if event["name"] == old_event["name"]), None
            )
            updated_event = {
                **old_event,
                "description": matching_new_event["description"].get("ja", ""),
                "href": matching_new_event.get("href", ""),
                "javadoc": matching_new_event.get("javadoc", ""),
                "link": matching_new_event.get("link", ""),
                "source": matching_new_event.get("source", ""),
                "priority": old_event.get("priority", "NORMAL"),
            }
            modified_events.append(updated_event)
            updated_events.append(updated_event)
        else:
            removed_events.append(old_event)

    # Adding new events
    for new_event in events_new["events"]:
        if new_event["name"] not in {event["name"] for event in events_old}:
            if not is_compatible_event(new_event):
                continue
            added_event = {
                "description": new_event["description"].get("ja", ""),
                "abstract": new_event.get("abstract", False),
                "href": new_event.get("href", ""),
                "javadoc": new_event.get("javadoc", ""),
                "link": new_event.get("link", ""),
                "name": new_event["name"],
                "source": new_event.get("source", ""),
                "implemented": False,
                "priority": "NORMAL",
            }
            updated_events.append(added_event)
            added_events.append(added_event)

    # Print the changes
    if added_events:
        for event in added_events:
            print(f"+ {event["name"]}")
    if updated_events:
        for event in updated_events:
            print(f"↑ {event["name"]}")
    if removed_events:
        for event in removed_events:
            print(f"- {event["name"]}")

    # Sort the events by name
    updated_events.sort(key=lambda event: event["name"])

    with open("events.json", "w", encoding="utf-8") as f:
        json.dump(updated_events, f, ensure_ascii=False, indent=2)

    print("events.json has been updated.")


def main():
    try:
        events_new = fetch_events_new()
        events_old = fetch_events_old()
        backup_old_events()
        update_events(events_old, events_new)
    except Exception as e:
        print(f"An error occurred: {e}")


if __name__ == "__main__":
    main()
