/*
 * File:    Channel.java
 * Package: youtube
 * Author:  Zachary Gill
 */

package youtube;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

/**
 * Holds Channels and Playlists for the Youtube Channel Downloader.
 */
public enum Channel {
    
    // ----------------------------------------------------------------
    //
    //  To get the playlistId for a Youtube Playlist:
    //   1. Go to the Youtube Playlist
    //   2. Simply copy it from the url:
    //   https://www.youtube.com/watch?v=3qiLI1ILMlU&list=PLdE7uo_7KBkfAWkk7-Clm18krBuziKQfr
    //                                                   <PLdE7uo_7KBkfAWkk7-Clm18krBuziKQfr>
    //
    // To get the playlistId for a Youtube Channel:
    //   1. Go to the Youtube Channel
    //   2. View the Page Source
    //   3. Search for "externalId" and copy that value
    //   4. Replace the second character from a 'C' to a 'U'
    //
    // ----------------------------------------------------------------
    
    //Values
    
    //BEATS
    MUSIC_LAB_HACKER(true, "MusicLabHacker", "PLdE7uo_7KBkfAWkk7-Clm18krBuziKQfr", "Beats/Music Lab", true, "Beats/Hacker Music.m3u", false),
    MUSIC_LAB_WORK(true, "MusicLabWork", "PLdE7uo_7KBkc6L7Bgqzz_Q7q2JN2AqHV3", "Beats/Music Lab", true, "Beats/Work Music.m3u", false),
    MUSIC_LAB_CHILLSTEP(true, "MusicLabChillstep", "PLdE7uo_7KBkeH0adsnxZupMARfGxY6qik", "Beats/Music Lab", true, "Beats/Chillstep Music.m3u", false),
    MUSIC_LAB_CHILLOUT(true, "MusicLabChillout", "PLdE7uo_7KBkeSTmryNClNxUkioFpq3Btx", "Beats/Music Lab", true, "Beats/Chillout Music.m3u", false),
    MUSIC_LAB_AMBIENT(true, "MusicLabAmbient", "PLdE7uo_7KBketYRDDb8lheUOD4q7U27DR", "Beats/Music Lab", true, "Beats/Ambient Music.m3u", false),
    MUSIC_LAB_LOFI(false, "MusicLabLoFi", "PLdE7uo_7KBkcywzGlsZ9c6aerJr3tK058", "Beats/Music Lab", true, "Beats/Lo-Fi Music.m3u", false),
    MUSIC_LAB_CONTEMPORARY(false, "MusicLabContemporary", "PLdE7uo_7KBkdg39l-YqG9eXanlNBiSgzf", "Beats/Music Lab", true, "Beats/Contemporary Music.m3u", false),
    MUSIC_LAB_STUDY(true, "MusicLabStudy", "PLdE7uo_7KBkcx8_AcwRjTPiYSauQTQeNi", "Beats/Music Lab", true, "Beats/Study Music.m3u", false),
    MUSIC_LAB_CHILLHOP(false, "MusicLabChillhop", "PLdE7uo_7KBkdmK1rCN4D-GO9g79QXqdVd", "Beats/Music Lab", true, "Beats/Chillhop Music.m3u", false),
    MUSIC_LAB_ENERGY(true, "MusicLabEnergy", "PLdE7uo_7KBkcp_XPf2GiTOqUWAdvqM4ap", "Beats/Music Lab", true, "Beats/Energy Music.m3u", false),
    MUSIC_LAB_FUTURE_GARAGE(true, "MusicLabFutureGarage", "PLdE7uo_7KBkdbssGgnnIDm3EnE2gmHyEQ", "Beats/Music Lab", true, "Beats/Future Garage Music.m3u", false),
    JIMTV_PROGRAMMING(true, "JimTVProgramming", "PLUja9J5M1XReqoBal5IKog_PWz2Q_hZ7Y", "Beats/JimTV", true, "Beats/Programming Music.m3u", false),
    DREAMHOP_MUSIC(true, "DreamhopMusic", "UUz9_4daWw-uWuqeB6_IkhMg", "Beats/Dreamhop Music", true, "Beats/Dreamhop Music.m3u", false),
    
    //TRAP
    TRAP_CITY(true, "TrapCity", "UU65afEgL62PGFWXY7n6CUbA", "Trap/Trap City", true, "Trap/Trap City.m3u"),
    SKY_BASS(true, "SkyBass", "UUpXbwekw4ySNHGt26aAKvHQ", "Trap/Sky Bass", true, "Trap/Sky Bass.m3u"),
    TRAP_NATION(true, "TrapNation", "UUa10nxShhzNrCE1o2ZOPztg", "Trap/Trap Nation", true, "Trap/Trap Nation.m3u"),
    BASS_NATION(true, "BassNation", "UUCvVpbYRgYjMN7mG7qQN0Pg", "Trap/Bass Nation", true, "Trap/Bass Nation.m3u"),
    
    //MUSIC
    THE_COMET_IS_COMING(false, "TheCometIsComing", "PLqffNt5cY34WycBZsqhVoXgRnehbbxyTB", "Music/The Comet Is Coming", true, "My Playlists/The Comet Is Coming.m3u"),
    
    //RUNESCAPE
    OSRS_BEATZ(true, "OsrsBeatz", "UUs1rnF_c_VSg74M5CQ-HKWg", "Runescape/OSRS Beatz", true, "Runescape/OSRS Beatz/OSRS Beatz.m3u"),
    BY_RELEASE(true, "ByRelease", "PLDSJpYkJoHD-0Keg6Fu7bcVTlbR27T-88", "Youtube/Runescape/By Release", false, "Youtube/Runescape/By Release.m3u"),
    OSRS_CHALLENGES_TANZOO(true, "OsrsChallengesTanzoo", "PL-Ub6X6SpQG_CElz7Gt1lV_BOSCv5vWNU", "Youtube/Runescape/OSRS Challenges", false, "Youtube/Runescape/OSRS Challenges - Tanzoo.m3u", false),
    OSRS_CHALLENGES_VIRTOSO(true, "OsrsChallengesVirtoso", "PLkWQe8Xki9YgUGQSvwC7E7dxAtKDVZ0hb", "Youtube/Runescape/OSRS Challenges", false, "Youtube/Runescape/OSRS Challenges - Virtoso.m3u", false),
    MUDKIP_HCIM(true, "MudkipHcim", "PL4Ct8chrkvPgti4HYTZFo7FSfxlLlptbN", "Youtube/Runescape/HCIM - Mudkip", false, "Youtube/Runescape/HCIM - Mudkip.m3u"),
    MUDKIP_UIM(true, "MudkipUim", "PL4Ct8chrkvPitHm4U6QZfxrmDJVgg6VR1", "Youtube/Runescape/UIM - Mudkip", false, "Youtube/Runescape/UIM - Mudkip.m3u"),
    SWAMPLETICS(true, "Swampletics", "PLWiMc19-qaA3u1ZawZQIKAh0BknPvoK8a", "Youtube/Runescape/Swampletics", false, "Youtube/Runescape/Swampletics.m3u"),
    LOWER_THE_BETTER(true, "LowerTheBetter", "PLGCe4YMe1XHIVM8NE-RC7k3vSNfU72aO6", "Youtube/Runescape/Lower the Better", false, "Youtube/Runescape/Lower the Better.m3u"),
    OSRS_WEEKLY_RECAP(true, "OsrsWeeklyRecap", "PLiETVLquxFqxOaD4dT35ooeG9Ro0qK6LU", "Youtube/Runescape/Weekly Recap", false, "Youtube/Runescape/Weekly Recap.m3u"),
    OSRS_MARKET_ANALYSIS(true, "OsrsMarketAnalysis", "UUIi4nY4YuOYUJEg8XLM0vQw", "Youtube/Runescape/Market Analysis", false, "Youtube/Runescape/Market Analysis.m3u"),
    IRON_MAIN(true, "IronMain", "PLhsEAJsiNQ3afUF7AkQ_6RbH0Nicd1wCX", "Youtube/Runescape/IronMain", false, "Youtube/Runescape/Iron Main.m3u"),
    ONE_KICK_RICK(true, "OneKickRick", "PLhsEAJsiNQ3YMprz-CHZ_MCMxedsdANc2", "Youtube/Runescape/One Kick Rick", false, "Youtube/Runescape/One Kick Rick.m3u"),
    
    //D&D
    DND_LORE(true, "DndLore", "PL-Tj3kmYOOy2OVjoqTO8joausM-6JjcI-", "Youtube/D&D/Dungeons and Dragons Lore", false, "Youtube/D&D/Dungeons and Dragons Lore.m3u"),
    
    //SPACE
    PBS_SPACE_TIME(true, "PbsSpaceTime", "UU7_gcs09iThXybpVgjHZ_7g", "Youtube/Space/PBS Space Time", false, "Youtube/Space/PBS Space Time.m3u"),
    ISAAC_ARTHUR(true, "IsaacArthur", "UUZFipeZtQM5CKUjx6grh54g", "Youtube/Space/Isaac Arthur", false, "Youtube/Space/Isaac Arthur.m3u"),
    ISAAC_ARTHUR_P01(true, "IsaacArthurP01", "PLIIOUpOge0LuFZG2lvL9-zbxovZabYxcy", "Youtube/Space/Isaac Arthur", false, "Youtube/Space/Isaac Arthur - Becoming an Interplanetary Species.m3u", false),
    ISAAC_ARTHUR_P02(true, "IsaacArthurP02", "PLIIOUpOge0LulClL2dHXh8TTOnCgRkLdU", "Youtube/Space/Isaac Arthur", false, "Youtube/Space/Isaac Arthur - Fermi Paradox.m3u", false),
    ISAAC_ARTHUR_P03(true, "IsaacArthurP03", "PLIIOUpOge0LsIzYlIAIRdAGJTqAW6FmCE", "Youtube/Space/Isaac Arthur", false, "Youtube/Space/Isaac Arthur - Outward Bound.m3u", false),
    ISAAC_ARTHUR_P04(true, "IsaacArthurP04", "PLIIOUpOge0LvQYACAZwizb8gqtXL-10PC", "Youtube/Space/Isaac Arthur", false, "Youtube/Space/Isaac Arthur - Post Scarcity Civilizations.m3u", false),
    ISAAC_ARTHUR_P05(true, "IsaacArthurP05", "PLIIOUpOge0Ls3WMYP_2FpP9Y0mjgtf98M", "Youtube/Space/Isaac Arthur", false, "Youtube/Space/Isaac Arthur - Advanced Civilizations.m3u", false),
    ISAAC_ARTHUR_P06(true, "IsaacArthurP06", "PLIIOUpOge0Lu97HzMt_BJu36UMaItB1cm", "Youtube/Space/Isaac Arthur", false, "Youtube/Space/Isaac Arthur - Alien Civilizations.m3u", false),
    ISAAC_ARTHUR_P07(true, "IsaacArthurP07", "PLIIOUpOge0LsGJI_vni4xvfBQTuryTwlU", "Youtube/Space/Isaac Arthur", false, "Youtube/Space/Isaac Arthur - Upward Bound.m3u", false),
    ISAAC_ARTHUR_P08(true, "IsaacArthurP08", "PLIIOUpOge0Lv9Y_4Vmcgaxue0jyZG3_4K", "Youtube/Space/Isaac Arthur", false, "Youtube/Space/Isaac Arthur - Earth 2.0.m3u", false),
    ISAAC_ARTHUR_P09(true, "IsaacArthurP09", "PLIIOUpOge0Ls94qU9ZgTy3A-PbmRKKbV4", "Youtube/Space/Isaac Arthur", false, "Youtube/Space/Isaac Arthur - Visions of Earth.m3u", false),
    ISAAC_ARTHUR_P10(true, "IsaacArthurP10", "PLIIOUpOge0Lv5kr9vrX8DJjlF1A3QWJ3D", "Youtube/Space/Isaac Arthur", false, "Youtube/Space/Isaac Arthur - The Moon - Returning and Colonizing.m3u", false),
    ISAAC_ARTHUR_P11(true, "IsaacArthurP11", "PLIIOUpOge0LuzO1f6z-sCZFawM_xiMHCD", "Youtube/Space/Isaac Arthur", false, "Youtube/Space/Isaac Arthur - Fermi Paradox - Great Filters.m3u", false),
    ISAAC_ARTHUR_P12(true, "IsaacArthurP12", "PLIIOUpOge0LufQYxcfYVqcVQOFOHFynMl", "Youtube/Space/Isaac Arthur", false, "Youtube/Space/Isaac Arthur - Colonizing Space.m3u", false),
    ISAAC_ARTHUR_P13(true, "IsaacArthurP13", "PLIIOUpOge0LtBd4s7qojmayIXcB80x0qB", "Youtube/Space/Isaac Arthur", false, "Youtube/Space/Isaac Arthur - Rogue Civilizations.m3u", false),
    ISAAC_ARTHUR_P14(true, "IsaacArthurP14", "PLIIOUpOge0LtW77TNvgrWWu5OC3EOwqxQ", "Youtube/Space/Isaac Arthur", false, "Youtube/Space/Isaac Arthur - Megastructures.m3u", false),
    ISAAC_ARTHUR_P15(true, "IsaacArthurP15", "PLIIOUpOge0LvNm82I9n8CBaGUXoVeKbYn", "Youtube/Space/Isaac Arthur", false, "Youtube/Space/Isaac Arthur - Biotechnology.m3u", false),
    ISAAC_ARTHUR_P16(true, "IsaacArthurP16", "PLIIOUpOge0LuCndr25ORvBN9OI1k1O4kJ", "Youtube/Space/Isaac Arthur", false, "Youtube/Space/Isaac Arthur - Existential Crisis Series.m3u", false),
    ISAAC_ARTHUR_P17(true, "IsaacArthurP17", "PLIIOUpOge0Lt0pjc1LgiEQ1EpaIe_y_Jb", "Youtube/Space/Isaac Arthur", false, "Youtube/Space/Isaac Arthur - Cyborgs, Androids, Transhumanism, and AI.m3u", false),
    ISAAC_ARTHUR_P18(true, "IsaacArthurP18", "PLIIOUpOge0LvpLdGIp4xCyCVZEEUQ1Udn", "Youtube/Space/Isaac Arthur", false, "Youtube/Space/Isaac Arthur - Futuristic Weapons.m3u", false),
    ISAAC_ARTHUR_P19(true, "IsaacArthurP19", "PLIIOUpOge0LvLIXNMDKHf30gfyfnyX-4O", "Youtube/Space/Isaac Arthur", false, "Youtube/Space/Isaac Arthur - Cosmology.m3u", false),
    ISAAC_ARTHUR_P20(true, "IsaacArthurP20", "PLIIOUpOge0LvHsTP5fm8oxB1qPS54sTMk", "Youtube/Space/Isaac Arthur", false, "Youtube/Space/Isaac Arthur - Civilizations at the End of Time.m3u", false),
    ISAAC_ARTHUR_P21(true, "IsaacArthurP21", "PLIIOUpOge0Ls9qzKr4sp4Kys3NPXxBZ-C", "Youtube/Space/Isaac Arthur", false, "Youtube/Space/Isaac Arthur - Clarketech.m3u", false),
    ISAAC_ARTHUR_P22(true, "IsaacArthurP22", "PLIIOUpOge0Lv2jCm7LnYBiRYAPvxHyuWL", "Youtube/Space/Isaac Arthur", false, "Youtube/Space/Isaac Arthur - Faster than Light.m3u", false),
    ISAAC_ARTHUR_P23(true, "IsaacArthurP23", "PLIIOUpOge0LskSp9Jac9nE6xSDfZ83Tgf", "Youtube/Space/Isaac Arthur", false, "Youtube/Space/Isaac Arthur - Generation Ships and Interstellar Colonization.m3u", false),
    ISAAC_ARTHUR_P24(true, "IsaacArthurP24", "PLIIOUpOge0Lv3LmdudQ6aPFzEvJarKF2J", "Youtube/Space/Isaac Arthur", false, "Youtube/Space/Isaac Arthur - Black Holes.m3u", false),
    ISAAC_ARTHUR_P25(true, "IsaacArthurP25", "PLIIOUpOge0Lvr26RCeM_6mq72KFhPWEkG", "Youtube/Space/Isaac Arthur", false, "Youtube/Space/Isaac Arthur - Life in a Space Colony.m3u", false),
    ISAAC_ARTHUR_P27(true, "IsaacArthurP27", "PLIIOUpOge0LsnC7OipBrfLSewH76ENAgc", "Youtube/Space/Isaac Arthur", false, "Youtube/Space/Isaac Arthur - Interstellar Warfare and Worldbuilding.m3u", false),
    ISAAC_ARTHUR_P28(true, "IsaacArthurP28", "PLIIOUpOge0LuGoV8698AWjDcMYeB9OvEf", "Youtube/Space/Isaac Arthur", false, "Youtube/Space/Isaac Arthur - Habitable Planets.m3u", false),
    
    //SCIENCE
    KURZGESAGT(true, "Kurzgesagt", "UUsXVk37bltHxD1rDPwtNM8Q", "Youtube/Science/Kurzgesagt", false, "Youtube/Science/Kurzgesagt.m3u"),
    SCI_SHOW(true, "SciShow", "UUZYTClx2T1of7BRZ86-8fow", "Youtube/Science/SciShow", false, "Youtube/Science/SciShow.m3u"),
    VERITASIUM(true, "Veritasium", "UUHnyfMqiRRG1u-2MsSQLbXA", "Youtube/Science/Veritasium", false, "Youtube/Science/Veritasium.m3u"),
    STEVE_MOULD(true, "SteveMould", "UUEIwxahdLz7bap-VDs9h35A", "Youtube/Science/Steve Mould", false, "Youtube/Science/Steve Mould.m3u"),
    APPLIED_SCIENCE(true, "AppliedScience", "UUivA7_KLKWo43tFcCkFvydw", "Youtube/Science/Applied Science", false, "Youtube/Science/Applied Science.m3u"),
    ANSWERS_WITH_JOE(true, "AnswersWithJoe", "PLAnwfqs0VrqoWNqkWnLM65HS6BIOEFsMl", "Youtube/Science/Answers With Joe", false, "Youtube/Science/Answers With Joe.m3u"),
    VSAUCE(true, "Vsauce", "UU6nSFpj9HTCZ5t-N3Rm3-HA", "Youtube/Science/Vsauce", false, "Youtube/Science/Vsauce.m3u"),
    MIND_FIELD_S1(false, "MindFieldS1", "PLZRRxQcaEjA4qyEuYfAMCazlL0vQDkIj2", "Youtube/Science/Vsauce/Mind Field/Season 1", false, "Youtube/Science/Vsauce/Mind Field/Season 1.m3u", false),
    MIND_FIELD_S2(false, "MindFieldS2", "PLZRRxQcaEjA7wmh3Z6EQuOK9fm1CqnJCI", "Youtube/Science/Vsauce/Mind Field/Season 2", false, "Youtube/Science/Vsauce/Mind Field/Season 2.m3u", false),
    MIND_FIELD_S3(false, "MindFieldS3", "PLZRRxQcaEjA7LX19uAySGlc9hmprBxfEP", "Youtube/Science/Vsauce/Mind Field/Season 3", false, "Youtube/Science/Vsauce/Mind Field/Season 3.m3u", false),
    CGP_GREY(true, "CgpGrey", "UU2C_jShtL725hvbm1arSV9w", "Youtube/Science/CGP Grey", false, "Youtube/Science/CGP Grey.m3u"),
    CODYS_LAB(false, "CodysLab", "UUu6mSoMNzHQiBIOCkHUa2Aw", "Youtube/Science/Cody's Lab", false, "Youtube/Science/Cody's Lab.m3u"),
    SMARTER_EVERY_DAY(false, "SmarterEveryDay", "UU6107grRI4m0o2-emgoDnAA", "Youtube/Science/Smarter Every Day", false, "Youtube/Science/Smarter Every Day.m3u"),
    MINUTE_PHYSICS(true, "MinutePhysics", "UUUHW94eEFW7hkUMVaZz4eDg", "Youtube/Science/Minute Physics", false, "Youtube/Science/Minute Physics.m3u"),
    
    //NATURE
    BRAVE_WILDERNESS(false, "BraveWilderness", "UU6E2mP01ZLH_kbAyeazCNdg", "Youtube/Nature/Brave Wilderness", false, "Youtube/Nature/Brave Wilderness.m3u"),
    
    //TECHNOLOGY
    TECHNOLOGY_CONNECTIONS(true, "TechnologyConnections", "UUy0tKL1T7wFoYcxCe0xjN6Q", "Youtube/Technology/Technology Connections", false, "Youtube/Technology/Technology Connections.m3u"),
    TECHNOLOGY_CONNECTIONS_P01(true, "TechnologyConnectionsP01", "PLv0jwu7G_DFWBEyCKt4tKHIk8ez_pZS_P", "Youtube/Technology/Technology Connections", false, "Youtube/Technology/Technology Connections - Digital Sound and the Compact Disc.m3u", false),
    TECHNOLOGY_CONNECTIONS_P02(true, "TechnologyConnectionsP02", "PLv0jwu7G_DFVP0SGNlBiBtFVkV5LZ7SOU", "Youtube/Technology/Technology Connections", false, "Youtube/Technology/Technology Connections - The CED.m3u", false),
    TECHNOLOGY_CONNECTIONS_P03(true, "TechnologyConnectionsP03", "PLv0jwu7G_DFUrcyMYAkUPODENwP4gYCmf", "Youtube/Technology/Technology Connections", false, "Youtube/Technology/Technology Connections - Videotape Format War.m3u", false),
    TECHNOLOGY_CONNECTIONS_P04(true, "TechnologyConnectionsP04", "PLv0jwu7G_DFUYPuDoKWCUy33lL9LnMBGX", "Youtube/Technology/Technology Connections", false, "Youtube/Technology/Technology Connections - History of Artificial Sound.m3u", false),
    TECHNOLOGY_CONNECTIONS_P05(true, "TechnologyConnectionsP05", "PLv0jwu7G_DFUoByWSHHoSTlUIxY7VkJLi", "Youtube/Technology/Technology Connections", false, "Youtube/Technology/Technology Connections - The Story of Laserdisc.m3u", false),
    TECHNOLOGY_CONNECTIONS_P06(true, "TechnologyConnectionsP06", "PLv0jwu7G_DFXjqlPfxjewWzwGltyf3d0T", "Youtube/Technology/Technology Connections", false, "Youtube/Technology/Technology Connections - Tech Explorations.m3u", false),
    TECHNOLOGY_CONNECTIONS_P07(true, "TechnologyConnectionsP07", "PLv0jwu7G_DFUGEfwEl0uWduXGcRbT7Ran", "Youtube/Technology/Technology Connections", false, "Youtube/Technology/Technology Connections - Television.m3u", false),
    COMPUTERPHILE(true, "Computerphile", "UU9-y-6csu5WGm29I7JiwpnA", "Youtube/Technology/Computerphile", false, "Youtube/Technology/Computerphile.m3u"),
    ROBERT_MILES(true, "RobertMiles", "UULB7AzTwc6VFZrBsO2ucBMg", "Youtube/Technology/Robert Miles", false, "Youtube/Technology/Robert Miles.m3u"),
    
    //MATH
    THREE_BLUE_ONE_BROWN(true, "3Blue1Brown", "UUYO_jab_esuFRV4b17AJtAw", "Youtube/Math/3Blue1Brown", false, "Youtube/Math/3Blue1Brown.m3u"),
    NUMBERPHILE(true, "Numberphile", "UUoxcjq-8xIDTYp3uz647V5A", "Youtube/Math/Numberphile", false, "Youtube/Math/Numberphile.m3u"),
    ZACH_STAR(true, "ZachStar", "UUpCSAcbqs-sjEVfk_hMfY9w", "Youtube/Math/Zach Star", false, "Youtube/Math/Zach Star.m3u"),
    SIXTY_SYMBOLS(true, "SixtySymbols", "UUvBqzzvUBLCs8Y7Axb-jZew", "Youtube/Math/Sixty Symbols", false, "Youtube/Math/Sixty Symbols.m3u"),
    
    //ENGINEERING
    AVE(true, "Ave", "UUhWv6Pn_zP0rI6lgGt3MyfA", "Youtube/Engineering/AvE", false, "Youtube/Engineering/AvE.m3u"),
    STUFF_MADE_HERE(true, "StuffMadeHere", "UUj1VqrHhDte54oLgPG4xpuQ", "Youtube/Engineering/Stuff Made Here", false, "Youtube/Engineering/Stuff Made Here.m3u"),
    ELECTROBOOM(true, "Electroboom", "UUJ0-OtVpF0wOKEqT2Z1HEtA", "Youtube/Engineering/ElectroBOOM", false, "Youtube/Engineering/ElectroBOOM.m3u"),
    HOW_ITS_MADE(false, "HowItsMade", "UUWBkudOTaVbvkCBc0pyZFMA", "Youtube/Engineering/How its Made", false, "Youtube/Engineering/How its Made.m3u"),
    PRACTICAL_ENGINEERING(true, "PracticalEngineering", "UUMOqf8ab-42UUQIdVoKwjlQ", "Youtube/Engineering/Practical Engineering", false, "Youtube/Engineering/Practical Engineering.m3u"),
    NEW_MIND(true, "NewMind", "UU5_Y-BKzq1uW_2rexWkUzlA", "Youtube/Engineering/New Mind", false, "Youtube/Engineering/New Mind.m3u"),
    ADAM_SAVAGE_TESTED(false, "AdamSavageTested", "UUiDJtJKMICpb9B1qf7qjEOA", "Youtube/Engineering/Adam Savage - Tested", false, "Youtube/Engineering/Adam Savage - Tested.m3u"),
    ADAM_SAVAGE_ONE_DAY_BUILDS(false, "AdamSavageOneDayBuilds", "UUiDJtJKMICpb9B1qf7qjEOA", "Youtube/Engineering/Adam Savage - One Day Builds", false, "Youtube/Engineering/Adam Savage - One Day Builds.m3u"),
    THIS_OLD_TONY(true, "ThisOldTony", "UU5NO8MgTQKHAWXp6z8Xl7yQ", "Youtube/Engineering/This Old Tony", false, "Youtube/Engineering/This Old Tony.m3u"),
    LOCK_PICKING_LAWYER(true, "LockPickingLawyer", "UUm9K6rby98W8JigLoZOh6FQ", "Youtube/Engineering/Lock Picking Lawyer", false, "Youtube/Engineering/Lock Picking Lawyer.m3u"),
    
    //CHEMISTRY
    PERIODIC_VIDEOS(true, "PeriodicVideos", "UUtESv1e7ntJaLJYKIO1FoYw", "Youtube/Chemistry/Periodic Videos", false, "Youtube/Chemistry/Periodic Videos.m3u"),
    NILE_RED(true, "NileRed", "UUFhXFikryT4aFcLkLw2LBLA", "Youtube/Chemistry/Nile Red", false, "Youtube/Chemistry/Nile Red.m3u"),
    BIG_STACK_D(true, "BigStackD", "UUfNQkKS2AkN_BX36MdvY70Q", "Youtube/Chemistry/Big Stack D", false, "Youtube/Chemistry/Big Stack D.m3u"),
    
    //MEDICINE
    CHUBBYEMU(true, "Chubbyemu", "UUKOvOaJv4GK-oDqx-sj7VVg", "Youtube/Medicine/Chubbyemu", false, "Youtube/Medicine/Chubbyemu.m3u"),
    NHAT_BANG_SPA(false, "NhatBangSpa", "UUaXE_FFGt8rhOf8BhH7fAJw", "Youtube/Medicine/Popping", false, "Youtube/Medicine/Popping - Nhat Bang Spa.m3u", false),
    LIKE_YOU(false, "LikeYou", "UU3bVSowe2a0jK8NWNlMGVUg", "Youtube/Medicine/Popping", false, "Youtube/Medicine/Popping - Like You.m3u", false),
    
    //CRIME
    FORENSIC_FILES(false, "ForensicFiles", "UUVBTlb6_rQkWY99ZKi2oBMw", "Youtube/Crime/Forensic Files", false, "Youtube/Crime/Forensic Files.m3u"),
    FORENSIC_FILES_S01(false, "ForensicFilesS01", "PLQWkmlie7GRNlka2Q_cs79zJxQjuwRz8_", "Youtube/Crime/Forensic Files", false, "Youtube/Crime/Forensic Files - Season 1.m3u", false),
    FORENSIC_FILES_S02(false, "ForensicFilesS02", "PLQWkmlie7GRN75WevfXf7bqNnMhlKLNqt", "Youtube/Crime/Forensic Files", false, "Youtube/Crime/Forensic Files - Season 2.m3u", false),
    FORENSIC_FILES_S03(false, "ForensicFilesS03", "PLQWkmlie7GRM6GwaU1q6s3YVyGTpeddR7", "Youtube/Crime/Forensic Files", false, "Youtube/Crime/Forensic Files - Season 3.m3u", false),
    FORENSIC_FILES_S04(false, "ForensicFilesS04", "PLQWkmlie7GRP7BWVRsLnhmVhTzfThXQrU", "Youtube/Crime/Forensic Files", false, "Youtube/Crime/Forensic Files - Season 4.m3u", false),
    FORENSIC_FILES_S05(false, "ForensicFilesS05", "PLQWkmlie7GRM-J1IKHiHIhUqTYVQjxA0T", "Youtube/Crime/Forensic Files", false, "Youtube/Crime/Forensic Files - Season 5.m3u", false),
    FORENSIC_FILES_S06(false, "ForensicFilesS06", "PLQWkmlie7GRMGnesHh8V2KQip3_Yb71a3", "Youtube/Crime/Forensic Files", false, "Youtube/Crime/Forensic Files - Season 6.m3u", false),
    FORENSIC_FILES_S07(false, "ForensicFilesS07", "PLQWkmlie7GRM3f2nJj08yJbmK9yKdMeDr", "Youtube/Crime/Forensic Files", false, "Youtube/Crime/Forensic Files - Season 7.m3u", false),
    FORENSIC_FILES_S08(false, "ForensicFilesS08", "PLQWkmlie7GROYQHJtBx48rLKlJ1xEXnxx", "Youtube/Crime/Forensic Files", false, "Youtube/Crime/Forensic Files - Season 8.m3u", false),
    FORENSIC_FILES_S09(false, "ForensicFilesS09", "PLQWkmlie7GRMcFRYzIAzeF9NVTv6BJ4F1", "Youtube/Crime/Forensic Files", false, "Youtube/Crime/Forensic Files - Season 9.m3u", false),
    FORENSIC_FILES_S10(false, "ForensicFilesS10", "PLQWkmlie7GROKXnoCUTm2Ionm-CnWRY0Y", "Youtube/Crime/Forensic Files", false, "Youtube/Crime/Forensic Files - Season 10.m3u", false),
    FORENSIC_FILES_S11(false, "ForensicFilesS11", "PLQWkmlie7GRMdDjTGZL0gLjwNJLEp7fUd", "Youtube/Crime/Forensic Files", false, "Youtube/Crime/Forensic Files - Season 11.m3u", false),
    FORENSIC_FILES_S12(false, "ForensicFilesS12", "PLQWkmlie7GRMFlchCYYeht1CvGLfDpqKN", "Youtube/Crime/Forensic Files", false, "Youtube/Crime/Forensic Files - Season 12.m3u", false),
    FORENSIC_FILES_S13(false, "ForensicFilesS13", "PLQWkmlie7GROpzz1Tw9LNCnfZW0tL8Ew_", "Youtube/Crime/Forensic Files", false, "Youtube/Crime/Forensic Files - Season 13.m3u", false),
    FORENSIC_FILES_S14(false, "ForensicFilesS14", "PLQWkmlie7GRNy5SEfGqkLDXrALFpsjc2k", "Youtube/Crime/Forensic Files", false, "Youtube/Crime/Forensic Files - Season 14.m3u", false),
    
    //GME
    SUPERSTONK(true, "Superstonk", "UUI4EET9NJPWxUuXGlG6fxPA", "../Documents/Other/GME/AMAs", false, "../Documents/Other/GME/AMAs/AMAs.m3u", false),
    
    //TRIPPY
    FELIX_COSGRAVE(true, "FelixCosgrave", "UUO7fujFV_MuxTM0TuZrnE6Q", "Youtube/Trippy/Felix Cosgrave", false, "Youtube/Trippy/Felix Cosgrave.m3u"),
    
    //FUNNY
    CASUALLY_EXPLAINED(true, "CasuallyExplained", "UUr3cBLTYmIK9kY0F_OdFWFQ", "Youtube/Funny/Casually Explained", false, "Youtube/Funny/Casually Explained.m3u"),
    SAM_ONELLA_ACADEMY(true, "SamOnellaAcademy", "UU1DTYW241WD64ah5BFWn4JA", "Youtube/Funny/Sam O'Nella Academy", false, "Youtube/Funny/Sam O'Nella Academy.m3u"),
    ZEFRANK(true, "Zefrank", "UUVpankR4HtoAVtYnFDUieYA", "Youtube/Funny/Zefrank", false, "Youtube/Funny/Zefrank.m3u"),
    OZZY_MAN_REVIEWS(false, "OzzyManReviews", "UUeE3lj6pLX_gCd0Yvns517Q", "Youtube/Funny/Ozzy Man Reviews", false, "Youtube/Funny/Ozzy Man Reviews.m3u"),
    KITBOGA(true, "Kitboga", "UUm22FAXZMw1BaWeFszZxUKw", "Youtube/Funny/Kitboga", false, "Youtube/Funny/Kitboga.m3u"),
    MORE_KITBOGA(true, "MoreKitboga", "UUWpQycBnpPxyWY6bBhr4Rbw", "Youtube/Funny/Kitboga - Uncut", false, "Youtube/Funny/Kitboga - Uncut.m3u"),
    KITBOGA_HIGHLIGHTS(true, "KitbogaHighlights", "UUgmhcAFfPVx_AMSf89HGh_A", "Youtube/Funny/Kitboga - Highlights", false, "Youtube/Funny/Kitboga - Highlights.m3u"),
    
    //BEST CUBE
    BEST_CUBE_COUBOY(true, "BestCubeCouboy", "UUfU5Otc792c-lNXfFK8PM7Q", "Youtube/Best Cube/Best Cube - Couboy", false, "Youtube/Best Cube/Best Cube - Couboy.m3u"),
    BEST_CUBE_SPARTA(true, "BestCubeSparta", "UUqb-JGYhFaV_jkJE026YZyA", "Youtube/Best Cube/Best Cube - Sparta", false, "Youtube/Best Cube/Best Cube - Sparta.m3u"),
    SEXY_CUBE(true, "SexyCube", "UUI1JjfDCiMyeSnSXtAZL5zg", "Youtube/Best Cube/Sexy Cube", false, "Youtube/Best Cube/Sexy Cube.m3u"),
    ANIME_CUBE(true, "AnimeCube", "UU2M5ugO54csNiDOEb8pOvvg", "Youtube/Best Cube/Anime Cube", false, "Youtube/Best Cube/Anime Cube.m3u");
    
    
    //Constants
    
    /**
     * The drive to use for storage of downloaded files.
     */
    public final File storageDrive = new File("E:/");
    
    /**
     * The Music directory in the storage drive.
     */
    public final File musicDir = new File(storageDrive, "Music");
    
    /**
     * The Videos directory in the storage drive.
     */
    public final File videoDir = new File(storageDrive, "Videos");
    
    
    //Fields
    
    /**
     * A flag indicating whether or not a Channel is enabled or not.
     */
    public boolean active;
    
    /**
     * The name of the Channel.
     */
    public String name;
    
    /**
     * The Playlist ID of the Channel.
     */
    public String playlistId;
    
    /**
     * The output folder to store the videos that are downloaded from the Channel.
     */
    public File outputFolder;
    
    /**
     * A flag indicating whether or not to save the videos from the Channel as an mp3 file or not; mp4 otherwise.
     */
    public boolean saveAsMp3;
    
    /**
     * The playlist file to add mp3 files downloaded from the Channel to if saving as mp3s; or null.
     */
    public File playlistFile;
    
    /**
     * A flag indicating whether or not to delete files from the output directory that are not in the playlist anymore.
     */
    public boolean keepClean;
    
    
    //Constructors
    
    /**
     * Constructs a new Channel.
     *
     * @param active       Whether the Channel is enabled or not.
     * @param name         The name of the Channel.
     * @param playlistId   The Playlist ID of the Channel.
     * @param outputFolder The output folder for the Channel.
     * @param saveAsMp3    Whether the Channel should download as mp3 or not.
     * @param playlistFile The playlist file for the Channel.
     * @param keepClean    Whether the Channel should keep its output directory clean or not.
     */
    Channel(boolean active, String name, String playlistId, String outputFolder, boolean saveAsMp3, String playlistFile, boolean keepClean) {
        this.active = active;
        this.name = name;
        this.playlistId = playlistId;
        this.outputFolder = new File(saveAsMp3 ? musicDir : videoDir, outputFolder);
        this.saveAsMp3 = saveAsMp3;
        this.playlistFile = (playlistFile != null) ? new File(saveAsMp3 ? musicDir : videoDir, playlistFile) : null;
        this.keepClean = keepClean;
    }
    
    /**
     * Constructs a new Channel.
     *
     * @param active       Whether the Channel is enabled or not.
     * @param name         The name of the Channel.
     * @param playlistId   The Playlist ID of the Channel.
     * @param outputFolder The output folder for the Channel.
     * @param saveAsMp3    Whether the Channel should download as mp3 or not.
     * @param playlistFile The playlist file for the Channel.
     */
    Channel(boolean active, String name, String playlistId, String outputFolder, boolean saveAsMp3, String playlistFile) {
        this(active, name, playlistId, outputFolder, saveAsMp3, playlistFile, true);
    }
    
    /**
     * Constructs a new Channel.
     *
     * @param active       Whether the Channel is enabled or not.
     * @param name         The name of the Channel.
     * @param playlistId   The Playlist ID of the Channel.
     * @param outputFolder The output folder for the Channel.
     * @param saveAsMp3    Whether the Channel should download as mp3 or not.
     * @see #Channel(boolean, String, String, String, boolean)
     */
    Channel(boolean active, String name, String playlistId, String outputFolder, boolean saveAsMp3) {
        this(active, name, playlistId, outputFolder, saveAsMp3, null);
    }
    
    
    //Methods
    
    /**
     * Returns whether the Channel is a Playlist or not.
     *
     * @return Whether the Channel is a Playlist or not.
     */
    public boolean isPlaylist() {
        return playlistId.startsWith("PL");
    }
    
    /**
     * Returns whether the Channel is a Channel or not.
     *
     * @return Whether the Channel is a Channel or not.
     */
    public boolean isChannel() {
        return playlistId.startsWith("UU");
    }
    
    
    //Functions
    
    /**
     * Performs special checks specific to a Channel before producing the queue.
     * Typically renaming.
     *
     * @param channel  The Channel.
     * @param videoMap The video map.
     * @param queue    The list of queued videos.
     * @param save     The list of saved videos.
     * @param blocked  The list of blocked videos.
     * @throws Exception When there is an error.
     */
    public static void performSpecialPreConditions(Channel channel, Map<String, YoutubeChannelDownloader.Video> videoMap, List<String> queue, List<String> save, List<String> blocked) throws Exception {
        switch (channel) {
            case JIMTV_PROGRAMMING:
                videoMap.forEach((key, value) -> {
                    String oldTitle = value.title;
                    String newTitle = oldTitle
                            .replace("Programming - Coding - Hacking music vol.", "Volume ")
                            .replace(" (", " - ")
                            .replace(")", "");
                    newTitle = YoutubeUtils.cleanTitle(newTitle);
                    File newOutput = new File(value.output.getParentFile(), value.output.getName().replace(oldTitle, newTitle));
                    if (value.output.exists() && !oldTitle.equals(newTitle)) {
                        try {
                            FileUtils.moveFile(value.output, newOutput);
                        } catch (IOException ignored) {
                        }
                    }
                    value.output = newOutput;
                    value.title = newTitle;
                });
                break;
            
            case BY_RELEASE:
                Pattern byReleaseNamePattern = Pattern.compile("^(?<title>.+)\\s*-\\s*By\\sRelease\\s*-?-\\s(?<episode>\\d+)$");
                videoMap.forEach((key, value) -> {
                    String oldTitle = value.title;
                    Matcher byReleaseNameMatcher = byReleaseNamePattern.matcher(oldTitle);
                    if (byReleaseNameMatcher.matches()) {
                        String newTitle = "By Release - " + byReleaseNameMatcher.group("episode") + " - " + byReleaseNameMatcher.group("title");
                        newTitle = YoutubeUtils.cleanTitle(newTitle);
                        File newOutput = new File(value.output.getParentFile(), value.output.getName().replace(oldTitle, newTitle));
                        if (value.output.exists() && !oldTitle.equals(newTitle)) {
                            try {
                                FileUtils.moveFile(value.output, newOutput);
                            } catch (IOException ignored) {
                            }
                        }
                        value.output = newOutput;
                        value.title = newTitle;
                    }
                });
                break;
            case OSRS_CHALLENGES_TANZOO:
                Pattern tanzooPattern = Pattern.compile("^(?<title>.+)\\s*-*\\s*Episode\\s*(?<episode>\\d+)$");
                videoMap.forEach((key, value) -> {
                    String oldTitle = value.title;
                    String newTitle = oldTitle
                            .replaceAll("Tanzoo\\s*vs?\\.?\\s*Virtoso\\s*-", "")
                            .replaceAll("-\\s*Challenge\\s*Episodes?", "- Episode")
                            .replaceAll("(?:OSRS|Osrs|osrs)\\s*Challenges?\\s*-?", "")
                            .replaceAll("\\s+", " ")
                            .replaceAll("(^\\s*-\\s*)|(\\s*-\\s*$)", "")
                            .replaceAll("\\s*Special\\s*$", "")
                            .replaceAll("(^\\s*-\\s*)|(\\s*-\\s*$)", "")
                            .replaceAll("\\s+", " ")
                            .trim();
                    Matcher tanzooMatcher = tanzooPattern.matcher(newTitle);
                    if (tanzooMatcher.matches()) {
                        newTitle = tanzooMatcher.group("title").replaceAll("\\s*-\\s*$", "").trim();
                    }
                    newTitle = "OSRS Challenges - " + newTitle + " (Tanzoo)";
                    newTitle = YoutubeUtils.cleanTitle(newTitle);
                    File newOutput = new File(value.output.getParentFile(), value.output.getName().replace(oldTitle, newTitle));
                    if (value.output.exists() && !oldTitle.equals(newTitle)) {
                        try {
                            FileUtils.moveFile(value.output, newOutput);
                        } catch (IOException ignored) {
                        }
                    }
                    value.output = newOutput;
                    value.title = newTitle;
                });
                break;
            case OSRS_CHALLENGES_VIRTOSO:
                Pattern virtosoPattern = Pattern.compile("^(?<title>.+)\\s*-*\\s*(Episode|EP\\.|Ep\\.)\\s*(?<episode>\\d+)$");
                videoMap.forEach((key, value) -> {
                    String oldTitle = value.title;
                    String newTitle = oldTitle
                            .replaceAll("Tanzoo\\s*vs?\\.?\\s*Virtoso\\s*-", "")
                            .replaceAll("\\s*-?\\sRunescape\\s2007\\s*-?", "")
                            .replaceAll("-\\s*Challenge\\s*Episodes?", "- Episode")
                            .replaceAll("(?:OSRS|Osrs|osrs)\\s*Challenges?\\s*-?", "")
                            .replaceAll("^\\s*-\\s*", "")
                            .replaceAll("\\s+", " ")
                            .replaceAll("(^\\s*-\\s*)|(\\s*-\\s*$)", "")
                            .replaceAll("\\s*Special\\s*$", "")
                            .replaceAll("(^\\s*-\\s*)|(\\s*-\\s*$)", "")
                            .replaceAll("\\s+", " ")
                            .trim();
                    Matcher virtosoMatcher = virtosoPattern.matcher(newTitle);
                    if (virtosoMatcher.matches()) {
                        newTitle = virtosoMatcher.group("title").replaceAll("\\s*-\\s*$", "").trim();
                    }
                    newTitle = "OSRS Challenges - " + newTitle + " (Virtoso)";
                    newTitle = YoutubeUtils.cleanTitle(newTitle);
                    File newOutput = new File(value.output.getParentFile(), value.output.getName().replace(oldTitle, newTitle));
                    if (value.output.exists() && !oldTitle.equals(newTitle)) {
                        try {
                            FileUtils.moveFile(value.output, newOutput);
                        } catch (IOException ignored) {
                        }
                    }
                    value.output = newOutput;
                    value.title = newTitle;
                });
                break;
            case MUDKIP_HCIM:
                Pattern hcimPattern1 = Pattern.compile("^(HCIM\\s(?<episode>\\d+)-).*");
                Pattern hcimPattern2 = Pattern.compile("^(?<title>.+)\\s(?:-\\s|\\(|)HCIM\\s*(?:Episode|ep\\.|Ep\\.|)\\s*(?<episode>\\d*\\.?\\d+)\\)?\\s*(?<level>\\(\\d+-\\d+\\))?");
                Pattern hcimPattern3 = Pattern.compile("^(?<title>.+)\\s(?:-\\s|\\(|)-\\s(?<episode>\\d*\\.?\\d+)\\)?");
                videoMap.forEach((key, value) -> {
                    String oldTitle = value.title;
                    String newTitle = oldTitle.replace("[OSRS] ", "");
                    value.output = new File(value.output.getParentFile(), value.output.getName().replace(oldTitle, newTitle));
                    oldTitle = newTitle;
                    if (newTitle.contains("Maxed HCIM")) {
                        newTitle = newTitle.replace("Maxed HCIM ", "");
                        Matcher hcimMatcher3 = hcimPattern3.matcher(newTitle);
                        if (hcimMatcher3.matches()) {
                            newTitle = "Maxed HCIM - " + hcimMatcher3.group("episode") + " - " + hcimMatcher3.group("title");
                        } else {
                            newTitle = newTitle.replaceAll("^Maxed HCIM ", "Maxed HCIM - ");
                        }
                    } else {
                        Matcher hcimMatcher1 = hcimPattern1.matcher(newTitle);
                        if (hcimMatcher1.matches()) {
                            newTitle = newTitle.replace(hcimMatcher1.group(1), hcimMatcher1.group(1).replace("-", " -"));
                        } else {
                            Matcher hcimMatcher2 = hcimPattern2.matcher(newTitle);
                            if (hcimMatcher2.matches()) {
                                newTitle = "HCIM - " + hcimMatcher2.group("episode") + " - " + hcimMatcher2.group("title").trim() +
                                        (hcimMatcher2.group("level") == null ? "" : (" " + hcimMatcher2.group("level")));
                            }
                        }
                        newTitle = newTitle.replaceAll("^HCIM ", "HCIM - ");
                    }
                    newTitle = newTitle
                            .replace("##", "#")
                            .replace("#", "- ")
                            .replace("()", "")
                            .replace(" - (", " (")
                            .replace(" - - ", " - ");
                    newTitle = YoutubeUtils.cleanTitle(newTitle);
                    File newOutput = new File(value.output.getParentFile(), value.output.getName().replace(oldTitle, newTitle));
                    if (value.output.exists() && !oldTitle.equals(newTitle)) {
                        try {
                            FileUtils.moveFile(value.output, newOutput);
                        } catch (IOException ignored) {
                        }
                    }
                    value.output = newOutput;
                    value.title = newTitle;
                });
                break;
            case MUDKIP_UIM:
                Pattern uimPattern = Pattern.compile(".*?(\\s(?:\\(UIM\\s-\\s|\\(-\\s)(?<episode>\\d+)\\)).*");
                videoMap.forEach((key, value) -> {
                    String oldTitle = value.title;
                    String newTitle = oldTitle;
                    Matcher uimMatcher = uimPattern.matcher(newTitle);
                    if (uimMatcher.matches()) {
                        newTitle = "UIM - " + uimMatcher.group("episode") + " - " + newTitle.replace(uimMatcher.group(1), "");
                    } else {
                        newTitle = newTitle.replaceAll("^UIM ", "UIM - ");
                    }
                    newTitle = newTitle
                            .replace("##", "#")
                            .replace("#", "- ")
                            .replace(" - - ", " - ");
                    newTitle = YoutubeUtils.cleanTitle(newTitle);
                    File newOutput = new File(value.output.getParentFile(), value.output.getName().replace(oldTitle, newTitle));
                    if (value.output.exists() && !oldTitle.equals(newTitle)) {
                        try {
                            FileUtils.moveFile(value.output, newOutput);
                        } catch (IOException ignored) {
                        }
                    }
                    value.output = newOutput;
                    value.title = newTitle;
                });
                break;
            case SWAMPLETICS:
                Pattern swampleticsPattern = Pattern.compile(".*?(\\s(?:\\(Swampletics\\s-\\s|\\(-\\s)(?<episode>\\d+)\\)).*");
                videoMap.forEach((key, value) -> {
                    String oldTitle = value.title;
                    String newTitle = oldTitle;
                    Matcher swampleticsMatcher = swampleticsPattern.matcher(newTitle);
                    if (swampleticsMatcher.matches()) {
                        newTitle = "Swampletics - " + swampleticsMatcher.group("episode") + " - " + newTitle.replace(swampleticsMatcher.group(1), "");
                    }
                    newTitle = YoutubeUtils.cleanTitle(newTitle);
                    File newOutput = new File(value.output.getParentFile(), value.output.getName().replace(oldTitle, newTitle));
                    if (value.output.exists() && !oldTitle.equals(newTitle)) {
                        try {
                            FileUtils.moveFile(value.output, newOutput);
                        } catch (IOException ignored) {
                        }
                    }
                    value.output = newOutput;
                    value.title = newTitle;
                });
                break;
            case LOWER_THE_BETTER:
                Pattern lowerTheBetterPattern = Pattern.compile(".*(\\s*-\\s*Lower\\s[Tt]he\\sBetter\\s(?:Ep\\.\\s)?-\\s\\s?(?<episode>\\d+)).*");
                AtomicInteger lowerTheBetterCount = new AtomicInteger(0);
                videoMap.forEach((key, value) -> {
                    lowerTheBetterCount.incrementAndGet();
                    String oldTitle = value.title;
                    String newTitle = oldTitle;
                    if (!newTitle.toLowerCase().contains("lower the better")) {
                        newTitle += " - Lower the Better - " + lowerTheBetterCount.get();
                    }
                    Matcher lowerTheBetterMatcher = lowerTheBetterPattern.matcher(newTitle);
                    if (lowerTheBetterMatcher.matches()) {
                        newTitle = "Lower the Better - " + lowerTheBetterMatcher.group("episode") + " - " + newTitle.replace(lowerTheBetterMatcher.group(1), "");
                    }
                    newTitle = YoutubeUtils.cleanTitle(newTitle);
                    File newOutput = new File(value.output.getParentFile(), value.output.getName().replace(oldTitle, newTitle));
                    if (value.output.exists() && !oldTitle.equals(newTitle)) {
                        try {
                            FileUtils.moveFile(value.output, newOutput);
                        } catch (IOException ignored) {
                        }
                    }
                    value.output = newOutput;
                    value.title = newTitle;
                });
                break;
            case OSRS_WEEKLY_RECAP:
                Pattern osrsWeeklyRecapPattern = Pattern.compile(".*?(\\s*-*\\s*(-\\s\\d+\\s*-*\\s*)?(OSRS\\s)?Weekly\\sRecap[\\s\\d\\-!]*)");
                SimpleDateFormat osrsWeeklyRecapDate = new SimpleDateFormat("yyyy-MM-dd");
                videoMap.forEach((key, value) -> {
                    String oldTitle = value.title;
                    String newTitle = oldTitle.replaceAll("\\s*\\[OSRS]", "");
                    Matcher osrsWeeklyRecapMatcher = osrsWeeklyRecapPattern.matcher(newTitle);
                    if (osrsWeeklyRecapMatcher.matches()) {
                        newTitle = "Weekly Recap - " + osrsWeeklyRecapDate.format(value.date) + " - " + newTitle.replace(osrsWeeklyRecapMatcher.group(1), "");
                    }
                    newTitle = YoutubeUtils.cleanTitle(newTitle);
                    File newOutput = new File(value.output.getParentFile(), value.output.getName().replace(oldTitle, newTitle));
                    if (value.output.exists() && !oldTitle.equals(newTitle)) {
                        try {
                            FileUtils.moveFile(value.output, newOutput);
                        } catch (IOException ignored) {
                        }
                    }
                    value.output = newOutput;
                    value.title = newTitle;
                });
                break;
            case IRON_MAIN:
                Pattern ironMainPattern = Pattern.compile(".*?(\\s*-\\s*(?:IronMain\\s)?\\[-\\s\\s*(?<episode>\\d+)]).*");
                AtomicInteger ironMainCount = new AtomicInteger(0);
                videoMap.forEach((key, value) -> {
                    ironMainCount.incrementAndGet();
                    String oldTitle = value.title;
                    String newTitle = oldTitle;
                    Matcher ironMainMatcher = ironMainPattern.matcher(newTitle);
                    if (ironMainMatcher.matches()) {
                        newTitle = newTitle.replace(ironMainMatcher.group(1), "");
                    }
                    newTitle = "IronMain - " + ironMainCount.get() + " - " + newTitle;
                    newTitle = YoutubeUtils.cleanTitle(newTitle);
                    File newOutput = new File(value.output.getParentFile(), value.output.getName().replace(oldTitle, newTitle));
                    if (value.output.exists() && !oldTitle.equals(newTitle)) {
                        try {
                            FileUtils.moveFile(value.output, newOutput);
                        } catch (IOException ignored) {
                        }
                    }
                    value.output = newOutput;
                    value.title = newTitle;
                });
                break;
            case ONE_KICK_RICK:
                Pattern oneKickRickPattern = Pattern.compile(".*(\\s*-\\s*Lumbridge-Draynor\\s(?:Only\\s)?HCIM\\s-\\s(?:One\\sKick\\sRick\\s-\\s)?(?:Episode|Ep\\.|ep\\.)\\s*(?:-\\s)?(?<episode>\\d+)).*");
                videoMap.forEach((key, value) -> {
                    String oldTitle = value.title;
                    String newTitle = oldTitle;
                    if (newTitle.contains("Series Trailer")) {
                        newTitle = newTitle.replace("Series Trailer", "One Kick Rick - ep.0");
                    }
                    Matcher oneKickRickMatcher = oneKickRickPattern.matcher(newTitle);
                    if (oneKickRickMatcher.matches()) {
                        newTitle = "One Kick Rick - " + oneKickRickMatcher.group("episode") + " - " + newTitle.replace(oneKickRickMatcher.group(1), "");
                    }
                    newTitle = YoutubeUtils.cleanTitle(newTitle);
                    File newOutput = new File(value.output.getParentFile(), value.output.getName().replace(oldTitle, newTitle));
                    if (value.output.exists() && !oldTitle.equals(newTitle)) {
                        try {
                            FileUtils.moveFile(value.output, newOutput);
                        } catch (IOException ignored) {
                        }
                    }
                    value.output = newOutput;
                    value.title = newTitle;
                });
                break;
            
            case STEVE_MOULD:
                videoMap.forEach((key, value) -> {
                    if (value.title.toLowerCase().contains("fewer than tom") ||
                            value.title.toLowerCase().contains("more than tom")) {
                        String oldTitle = value.title;
                        String newTitle = "This Video Has...";
                        newTitle = YoutubeUtils.cleanTitle(newTitle);
                        File newOutput = new File(value.output.getParentFile(), value.output.getName().replace(oldTitle, newTitle));
                        if (value.output.exists() && !oldTitle.equals(newTitle)) {
                            try {
                                FileUtils.moveFile(value.output, newOutput);
                            } catch (IOException ignored) {
                            }
                        }
                        value.output = newOutput;
                        value.title = newTitle;
                    }
                });
                break;
            case MIND_FIELD_S1:
                Pattern mindFieldS1Pattern = Pattern.compile(".*?(\\s*-\\s*(?:Mind\\sField\\s)\\(Ep\\.?\\s*(?<episode>\\d+)\\)).*");
                AtomicInteger mindFieldS1Count = new AtomicInteger(0);
                videoMap.forEach((key, value) -> {
                    mindFieldS1Count.incrementAndGet();
                    String oldTitle = value.title;
                    String newTitle = oldTitle;
                    Matcher mindFieldS1Matcher = mindFieldS1Pattern.matcher(newTitle);
                    if (mindFieldS1Matcher.matches()) {
                        newTitle = newTitle.replace(mindFieldS1Matcher.group(1), "");
                    }
                    newTitle = "Mind Field - S01E0" + mindFieldS1Count.get() + " - " + newTitle;
                    newTitle = YoutubeUtils.cleanTitle(newTitle);
                    value.output = new File(value.output.getParentFile(), value.output.getName().replace(oldTitle, newTitle));
                    value.title = newTitle;
                });
                break;
            case MIND_FIELD_S2:
                Pattern mindFieldS2Pattern = Pattern.compile(".*?(\\s*-\\s*(?:Mind\\sField\\sS2\\s)\\(Ep\\.?\\s*(?<episode>\\d+)\\)).*");
                AtomicInteger mindFieldS2Count = new AtomicInteger(0);
                videoMap.forEach((key, value) -> {
                    mindFieldS2Count.incrementAndGet();
                    String oldTitle = value.title;
                    String newTitle = oldTitle;
                    Matcher mindFieldS2Matcher = mindFieldS2Pattern.matcher(newTitle);
                    if (mindFieldS2Matcher.matches()) {
                        newTitle = newTitle.replace(mindFieldS2Matcher.group(1), "");
                    }
                    newTitle = "Mind Field - S02E0" + mindFieldS2Count.get() + " - " + newTitle;
                    newTitle = YoutubeUtils.cleanTitle(newTitle);
                    value.output = new File(value.output.getParentFile(), value.output.getName().replace(oldTitle, newTitle));
                    value.title = newTitle;
                });
                break;
            case MIND_FIELD_S3:
                AtomicInteger mindFieldS3Count = new AtomicInteger(0);
                videoMap.forEach((key, value) -> {
                    mindFieldS3Count.incrementAndGet();
                    String oldTitle = value.title;
                    String newTitle = oldTitle;
                    newTitle = "Mind Field - S03E0" + mindFieldS3Count.get() + " - " + newTitle;
                    newTitle = YoutubeUtils.cleanTitle(newTitle);
                    value.output = new File(value.output.getParentFile(), value.output.getName().replace(oldTitle, newTitle));
                    value.title = newTitle;
                });
                break;
            
            case LOCK_PICKING_LAWYER:
                videoMap.forEach((key, value) -> {
                    String oldTitle = value.title;
                    String newTitle = oldTitle.replace("[", "").replace("]", " -");
                    newTitle = YoutubeUtils.cleanTitle(newTitle);
                    value.output = new File(value.output.getParentFile(), value.output.getName().replace(oldTitle, newTitle));
                    value.title = newTitle;
                });
                break;
            
            case NHAT_BANG_SPA:
                videoMap.forEach((key, value) -> {
                    String oldTitle = value.title;
                    String newTitle = oldTitle.replace("010", "10");
                    newTitle = YoutubeUtils.cleanTitle(newTitle);
                    value.output = new File(value.output.getParentFile(), value.output.getName().replace(oldTitle, newTitle));
                    value.title = newTitle;
                });
                break;
            
            case FORENSIC_FILES:
            case FORENSIC_FILES_S01:
            case FORENSIC_FILES_S02:
            case FORENSIC_FILES_S03:
            case FORENSIC_FILES_S04:
            case FORENSIC_FILES_S05:
            case FORENSIC_FILES_S06:
            case FORENSIC_FILES_S07:
            case FORENSIC_FILES_S08:
            case FORENSIC_FILES_S09:
            case FORENSIC_FILES_S10:
            case FORENSIC_FILES_S11:
            case FORENSIC_FILES_S12:
            case FORENSIC_FILES_S13:
            case FORENSIC_FILES_S14:
                videoMap.forEach((key, value) -> {
                    String oldTitle = value.title;
                    String newTitle = oldTitle
                            .replace("( ", "(")
                            .replace(" )", ")")
                            .replace("Medical Detectives (Forensic Files)", "Forensic Files")
                            .replace(" in HD ", " ")
                            .replace("- ", " - ")
                            .replace(" , ", ", ")
                            .replace("Season ", "S")
                            .replace(", Ep ", "E")
                            .replaceAll("\\sS(\\d)E", " S0$1E")
                            .replaceAll("E(\\d)\\s*-", "E0$1 - ")
                            .replaceAll("\\s+", " ");
                    if (newTitle.equals("Forensic Files - Series Premiere - The Disappearance of Helle Crafts")) {
                        newTitle = "Forensic Files - S01E01 - The Disappearance of Helle Crafts";
                    }
                    newTitle = YoutubeUtils.cleanTitle(newTitle);
                    File newOutput = new File(value.output.getParentFile(), value.output.getName().replace(oldTitle, newTitle));
                    if (value.output.exists() && !oldTitle.equals(newTitle)) {
                        try {
                            FileUtils.moveFile(value.output, newOutput);
                        } catch (IOException ignored) {
                        }
                    }
                    value.output = newOutput;
                    value.title = newTitle;
                });
                if (channel == FORENSIC_FILES_S01) {
                    YoutubeChannelDownloader.Video video = new YoutubeChannelDownloader.Video();
                    video.videoId = "OZc6vcGjknI";
                    video.title = YoutubeUtils.cleanTitle("Forensic Files - S01E01 - The Disappearance of Helle Crafts");
                    video.url = YoutubeUtils.VIDEO_BASE + video.videoId;
                    video.date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2015-01-23 12:15:00");
                    video.output = new File(channel.outputFolder, video.title + (channel.saveAsMp3 ? ".mp3" : ".mp4"));
                    HashMap<String, YoutubeChannelDownloader.Video> tmp = new LinkedHashMap<>(videoMap);
                    videoMap.clear();
                    videoMap.put(video.videoId, video);
                    videoMap.putAll(tmp);
                }
                break;
        }
    }
    
    /**
     * Performs special checks specific to a Channel after producing the queue.
     * Typically filtering.
     *
     * @param channel  The Channel.
     * @param videoMap The video map.
     * @param queue    The list of queued videos.
     * @param save     The list of saved videos.
     * @param blocked  The list of blocked videos.
     * @throws Exception When there is an error.
     */
    public static void performSpecialPostConditions(Channel channel, Map<String, YoutubeChannelDownloader.Video> videoMap, List<String> queue, List<String> save, List<String> blocked) throws Exception {
        switch (channel) {
            case MUSIC_LAB_HACKER:
            case MUSIC_LAB_WORK:
            case MUSIC_LAB_CHILLSTEP:
            case MUSIC_LAB_CHILLOUT:
            case MUSIC_LAB_AMBIENT:
            case MUSIC_LAB_LOFI:
            case MUSIC_LAB_CONTEMPORARY:
            case MUSIC_LAB_STUDY:
            case MUSIC_LAB_CHILLHOP:
                videoMap.forEach((key, value) -> {
                    if (value.title.toLowerCase().contains("live 24-7")) {
                        if (!blocked.contains(key)) {
                            blocked.add(key);
                        }
                        queue.remove(key);
                    }
                });
                break;
            
            case OSRS_BEATZ:
                videoMap.forEach((key, value) -> {
                    if (!value.title.toLowerCase().contains("runescape")) {
                        if (!blocked.contains(key)) {
                            blocked.add(key);
                        }
                        queue.remove(key);
                    }
                });
                break;
            case OSRS_WEEKLY_RECAP:
                videoMap.forEach((key, value) -> {
                    if (!value.title.toLowerCase().contains("weekly recap")) {
                        if (!blocked.contains(key)) {
                            blocked.add(key);
                        }
                        queue.remove(key);
                    }
                });
                break;
            case OSRS_MARKET_ANALYSIS:
                videoMap.forEach((key, value) -> {
                    if (!value.title.toLowerCase().contains("market") && !value.title.toLowerCase().contains("economy")) {
                        if (!blocked.contains(key)) {
                            blocked.add(key);
                        }
                        queue.remove(key);
                    }
                });
                break;
            
            case ISAAC_ARTHUR:
            case ISAAC_ARTHUR_P01:
            case ISAAC_ARTHUR_P02:
            case ISAAC_ARTHUR_P03:
            case ISAAC_ARTHUR_P04:
            case ISAAC_ARTHUR_P05:
            case ISAAC_ARTHUR_P06:
            case ISAAC_ARTHUR_P07:
            case ISAAC_ARTHUR_P08:
            case ISAAC_ARTHUR_P09:
            case ISAAC_ARTHUR_P10:
            case ISAAC_ARTHUR_P11:
            case ISAAC_ARTHUR_P12:
            case ISAAC_ARTHUR_P13:
            case ISAAC_ARTHUR_P14:
            case ISAAC_ARTHUR_P15:
            case ISAAC_ARTHUR_P16:
            case ISAAC_ARTHUR_P17:
            case ISAAC_ARTHUR_P18:
            case ISAAC_ARTHUR_P19:
            case ISAAC_ARTHUR_P20:
            case ISAAC_ARTHUR_P21:
            case ISAAC_ARTHUR_P22:
            case ISAAC_ARTHUR_P23:
            case ISAAC_ARTHUR_P24:
            case ISAAC_ARTHUR_P25:
            case ISAAC_ARTHUR_P27:
            case ISAAC_ARTHUR_P28:
                videoMap.forEach((key, value) -> {
                    if (value.title.toLowerCase().contains("livestream") ||
                            value.title.toLowerCase().contains("hades") ||
                            value.title.equalsIgnoreCase("in the beginning") ||
                            value.title.toLowerCase().contains("collab") || value.title.toLowerCase().contains("colab")) {
                        if (!blocked.contains(key)) {
                            blocked.add(key);
                        }
                        queue.remove(key);
                    }
                });
                break;
            
            case ANSWERS_WITH_JOE:
                videoMap.forEach((key, value) -> {
                    if (value.title.toLowerCase().contains("live stream")) {
                        if (!blocked.contains(key)) {
                            blocked.add(key);
                        }
                        queue.remove(key);
                    }
                });
                break;
            
            case VSAUCE:
                final Date vSauceOldest = new SimpleDateFormat("yyyy-MM-dd").parse("2011-10-15");
                videoMap.forEach((key, value) -> {
                    if (value.originalTitle.contains("#") || value.title.contains("DONG") || value.title.contains("Mind Field") || value.date.before(vSauceOldest)) {
                        if (!blocked.contains(key)) {
                            blocked.add(key);
                        }
                        queue.remove(key);
                    }
                });
                break;
            
            case ADAM_SAVAGE_ONE_DAY_BUILDS:
                videoMap.forEach((key, value) -> {
                    if (!value.title.toLowerCase().contains("one day build") ||
                            value.title.toLowerCase().contains("last call")) {
                        if (!blocked.contains(key)) {
                            blocked.add(key);
                        }
                        queue.remove(key);
                    }
                });
                break;
            
            case CHUBBYEMU:
                final Date chubbyEmuOldest = new SimpleDateFormat("yyyy-MM-dd").parse("2017-08-07");
                videoMap.forEach((key, value) -> {
                    if (value.date.before(chubbyEmuOldest)) {
                        if (!blocked.contains(key)) {
                            blocked.add(key);
                        }
                        queue.remove(key);
                    }
                });
                break;
            case LIKE_YOU:
                videoMap.forEach((key, value) -> {
                    if (value.title.toLowerCase().contains("photographer") ||
                            value.title.toLowerCase().contains("phone") ||
                            value.title.toLowerCase().contains("friend") ||
                            value.title.toLowerCase().contains("my son") ||
                            value.title.toLowerCase().contains("dogs")) {
                        if (!blocked.contains(key)) {
                            blocked.add(key);
                        }
                        queue.remove(key);
                    }
                });
                break;
            
            case BEST_CUBE_COUBOY:
            case BEST_CUBE_SPARTA:
                videoMap.forEach((key, value) -> {
                    if (!value.title.toLowerCase().contains("best cube") ||
                            value.title.toLowerCase().contains("best coub")) {
                        if (!blocked.contains(key)) {
                            blocked.add(key);
                        }
                        queue.remove(key);
                    }
                });
                break;
            case SEXY_CUBE:
                videoMap.forEach((key, value) -> {
                    if (!value.title.toLowerCase().contains("sexy cube") ||
                            value.title.toLowerCase().contains("sexy coub")) {
                        if (!blocked.contains(key)) {
                            blocked.add(key);
                        }
                        queue.remove(key);
                    }
                });
                break;
        }
    }
    
}