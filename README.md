# Travel Grade V2

Android photo color-grading app focused on realistic mountain/travel photography.

## V2 features
- Select up to 50 photos from the gallery
- Four presets: Travel Natural, Alpine Blue, Mountain Drama, Warm Landscape
- Preset intensity from 15% to 150%
- Before / After preview
- Batch export all selected photos
- Saves JPEGs to `Pictures/Travel Grade`
- Processing is local on-device; no photo upload/server required
- Designed to preserve a realistic photographic look rather than AI reconstruction

## Build
Open the `TravelGrade` folder in Android Studio and build the `app` module.

A GitHub Actions workflow is included at `.github/workflows/build-apk.yml` and builds `app-debug.apk` on pushes.

## Preset intent
The default Travel Natural preset follows the look requested in the conversation: neutralize grey-blue cast, recover cloud highlights, add restrained midtone contrast and vibrance, preserve atmospheric distance, gently darken the top of the sky and add a subtle vignette.
